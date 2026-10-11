package org.llgs.global.util;


import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DateUtil {

    public static boolean isBefore(@NotNull LocalTime localTime1, @NotNull LocalTime localTime2) {
        return Objects.requireNonNull(localTime1).isBefore(Objects.requireNonNull(localTime2));
    }

    public static boolean isBeforeEquals(@NotNull LocalTime localTime1, @NotNull LocalTime localTime2) {
        return isBefore(localTime1, localTime2) || Objects.requireNonNull(localTime1).equals(Objects.requireNonNull(localTime2));
    }

    public static boolean isAfter(@NotNull LocalTime localTime1, @NotNull LocalTime localTime2) {
        return Objects.requireNonNull(localTime1).isAfter(Objects.requireNonNull(localTime2));
    }

    public static boolean isAfterEquals(@NotNull LocalTime localTime1, @NotNull LocalTime localTime2) {
        return isAfter(localTime1, localTime2) || Objects.requireNonNull(localTime1).equals(Objects.requireNonNull(localTime2));
    }

    /**
     * @see #periodToCollection(LocalDate, LocalDate, boolean)
     */
    public static Collection<LocalDate> periodToCollection(@NotNull LocalDate startDate, @NotNull LocalDate endDate) {
        return periodToCollection(startDate, endDate, false);
    }

    public static Collection<Map.Entry<LocalDate, LocalDate>> periodToEntries(LocalDate startDate, LocalDate endDate, int period) {
        // nullable 체크
        if(Objects.isNull(startDate) || Objects.isNull(endDate)) {
            return Collections.emptyList();
        }

        // 입력값 오류체크
        if(endDate.isBefore(startDate)) {
            return Collections.emptyList();
        }

        Collection<Map.Entry<LocalDate, LocalDate>> entries = new ArrayList<>();
        LocalDate sDate = startDate;
        while (endDate.isAfter(sDate) || endDate.isEqual(sDate)) {
            long diff = ChronoUnit.DAYS.between(sDate, endDate);
            long plusDays = diff > period ? period : diff;
            Map.Entry<LocalDate, LocalDate> entry = new AbstractMap.SimpleEntry<>(sDate, sDate.plusDays(plusDays));
            entries.add(entry);
            sDate = sDate.plusDays(plusDays);

            if(sDate.isEqual(endDate)) {
                break;
            }
        }

        return entries;
    }

    /**
     * 시작 & 종료일자 배열
     * @param startDate 시작일자
     * @param endDate 종료일자
     * @return 시작 & 종료범위 일자목록
     */
    public static Collection<LocalDate> periodToCollection(@NotNull LocalDate startDate, @NotNull LocalDate endDate, boolean isExcludeHoliday) {
        // nullable 체크
        if(Objects.isNull(startDate) || Objects.isNull(endDate)) {
            return Collections.emptyList();
        }

        // 입력값 오류체크
        if(0 > ChronoUnit.DAYS.between(startDate, endDate)) {
            return Collections.emptyList();
        }

        Stream<LocalDate> dateStream = startDate.datesUntil(endDate.plusDays(1)); // 검색지정일까지 범위에 포함되도록 +1일 추가
        if(isExcludeHoliday) {
            return dateStream.filter(localDate -> {
                DayOfWeek dayOfWeek = localDate.getDayOfWeek();
                return !(Objects.equals(DayOfWeek.SATURDAY, dayOfWeek) || Objects.equals(DayOfWeek.SUNDAY, dayOfWeek));
            }).collect(Collectors.toCollection(TreeSet::new));
        } else {
            return dateStream.collect(Collectors.toCollection(TreeSet::new));
        }
    }

    public static boolean isHoliday(LocalDate localDate) {
        return Optional.of(localDate.getDayOfWeek()).map(dayOfWeek -> DayOfWeek.SATURDAY.equals(dayOfWeek) || DayOfWeek.SUNDAY.equals(dayOfWeek)).orElse(false);
    }
}
