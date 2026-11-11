package com.pachi.aredl.service;

import com.pachi.aredl.model.TrackerResult;
import com.pachi.aredl.model.changelog.ChangelogDocument;
import com.pachi.aredl.model.changelog.ChangelogEvent;
import com.pachi.aredl.model.changelog.ChangelogRecordState;
import com.pachi.aredl.model.changelog.ChangelogTag;
import com.pachi.aredl.model.country.CountryRecord;
import com.pachi.aredl.model.level.Level;
import com.pachi.aredl.model.level.TrackedLevel;
import com.pachi.aredl.model.player.Player;

import java.time.Instant;
import java.util.*;

public class ChangelogProcessor {

    public List<ChangelogEvent> process(
            ChangelogDocument document,
            TrackerResult trackerResult
    ) {
        if (!document.isInitialized()) {
            initializeBaseline(document, trackerResult);
            return List.of();
        }
        return detectNewRecords(document, trackerResult);
    }

    public void initializeBaseline(
            ChangelogDocument document,
            TrackerResult trackerResult
    ) {
        for (TrackedLevel trackedLevel : trackerResult.getTrackedLevels()) {
            Level level = trackedLevel.getLevel();

            for (CountryRecord record : trackedLevel.getRecords()){
                if (record.getId() == null) {
                    continue;
                }

                if (!document.getState()
                        .getKnownRecordIds()
                        .add(record.getId())) {
                    continue;
                }

                Player player = record.getSubmittedBy();

                String playerId = player != null
                        ? player.getId()
                        : null;

                if (playerId != null) {
                    document.getState()
                            .getKnownPlayerIds()
                            .add(playerId);
                }

                ChangelogRecordState recordState =
                        createRecordState(record, level, playerId);

                document.getState()
                        .getKnownRecords()
                        .add(recordState);
            }
        }

        document.setInitialized(true);
    }

    private List<ChangelogEvent> detectNewRecords(
            ChangelogDocument document,
            TrackerResult trackerResult
    ) {
        List<NewRecordCandidate> candidates = new ArrayList<>();

        // 1. Records nuevos.
        for (TrackedLevel trackedLevel : trackerResult.getTrackedLevels()) {
            for (CountryRecord record : trackedLevel.getRecords()){
                if (record.getId() == null) {
                    continue;
                }

                if (!document.getState()
                        .getKnownRecordIds()
                        .contains(record.getId())) {
                    candidates.add(
                            new NewRecordCandidate(
                                    record,
                                    trackedLevel.getLevel()
                            )
                    );
                }
            }
        }

        // 2. Orden cronológico achievedAt.
        candidates.sort(
                Comparator.comparing(
                        (NewRecordCandidate candidate) ->
                                candidate.record().getAchievedAt(),
                        Comparator.nullsLast(Comparator.naturalOrder())
                ).thenComparing(
                        candidate -> candidate.record().getId()
                )
        );

        List<ChangelogEvent> newEvents = new ArrayList<>();
        String detectedAt = Instant.now().toString();

        for (NewRecordCandidate candidate : candidates) {
            CountryRecord record = candidate.record();
            Level level = candidate.level();

            // Añadir ID
            if (!document.getState()
                    .getKnownRecordIds()
                    .add(record.getId())) {
                continue;
            }

            Player player = record.getSubmittedBy();

            String playerId = player != null
                    ? player.getId()
                    : null;

            if (playerId != null) {
                document.getState()
                        .getKnownPlayerIds()
                        .add(playerId);
            }

            ChangelogEvent event = new ChangelogEvent(
                    "NEW_COUNTRY_RECORD",
                    record.getId(),
                    level.getId(),
                    level.getName(),
                    level.getPosition(),
                    playerId,
                    player != null ? player.getUsername() : null,
                    player != null ? player.getGlobal_name() : null,
                    record.getAchievedAt(),
                    detectedAt,
                    new ArrayList<>()
            );

            document.getEvents().add(event);

            ChangelogRecordState recordState =
                    createRecordState(record, level, playerId);

            document.getState()
                    .getKnownRecords()
                    .add(recordState);

            newEvents.add(event);
        }

        recalculateSpecialEvents(document);
        return newEvents;
    }

    private void recalculateSpecialEvents(ChangelogDocument document) {

        List<ChangelogRecordState> timeline =
                new ArrayList<>(document.getState().getKnownRecords());

        timeline.sort(
                Comparator.comparing(
                        ChangelogRecordState::getAchievedAt,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ).thenComparing(
                        ChangelogRecordState::getRecordId,
                        Comparator.nullsLast(Comparator.naturalOrder())
                )
        );

        Map<String, ChangelogEvent> eventsByRecordId = new HashMap<>();
        for (ChangelogEvent event : document.getEvents()) {
            if (event.getRecordId() != null) {
                eventsByRecordId.put(event.getRecordId(), event);
            }
        }

        // Información acumulada de los récords anteriores.
        Set<String> seenPlayers = new HashSet<>();
        Set<String> seenLevels = new HashSet<>();

        Map<String, Integer> personalHardest = new HashMap<>();
        Integer countryHardest = null;

        for (ChangelogRecordState record : timeline) {

            String recordId = record.getRecordId();
            String playerId = record.getPlayerId();
            String levelId = record.getLevelId();
            Integer position = record.getLevelPosition();

            ChangelogEvent event = eventsByRecordId.get(recordId);

            if (event != null) {
                List<ChangelogTag> specialEvents = new ArrayList<>();

                boolean newPlayer = playerId != null
                        && !seenPlayers.contains(playerId);

                if (newPlayer) {
                    specialEvents.add(ChangelogTag.NEW_PLAYER);
                }

                if (levelId != null && !seenLevels.contains(levelId)) {
                    specialEvents.add(ChangelogTag.FIRST_COUNTRY_COMPLETION);
                }

                if (!newPlayer && playerId != null && position != null) {
                    Integer previousHardest = personalHardest.get(playerId);

                    if (previousHardest == null || position < previousHardest) {
                        specialEvents.add(ChangelogTag.NEW_PERSONAL_HARDEST);
                    }
                }

                if (position != null
                        && (countryHardest == null || position < countryHardest)) {
                    specialEvents.add(ChangelogTag.NEW_COUNTRY_HARDEST);
                }
                event.setSpecialEvents(specialEvents);
            }

            if (playerId != null) {
                seenPlayers.add(playerId);

                if (position != null) {
                    personalHardest.merge(playerId, position, Math::min);
                }
            }
            if (levelId != null) {
                seenLevels.add(levelId);
            }
            if (position != null
            && (countryHardest == null || position < countryHardest)) {
                countryHardest = position;
            }
        }

    }

    private ChangelogRecordState createRecordState(
            CountryRecord record,
            Level level,
            String playerId
    ) {
        return new ChangelogRecordState(
                record.getId(),
                level.getId(),
                level.getName(),
                level.getPosition(),
                playerId,
                record.getAchievedAt()
        );
    }

    private record NewRecordCandidate(
            CountryRecord record,
            Level level
    ) {}
}
