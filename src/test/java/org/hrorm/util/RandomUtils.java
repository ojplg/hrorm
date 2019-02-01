package org.hrorm.util;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Random generation utilities to help ensure tests are not dependent on magic values.
 * Aims is to make it easier for the developer while making testing more robust.
 */
public class RandomUtils {
    private static final Random RANDOM = new Random(System.currentTimeMillis());



    private RandomUtils() {
        // Don't construct me!
    }

    public static <E> List<E> randomNumberOf(int min, int max, Supplier<E> endlessSupplier) {
        return IntStream.range(0, range(min, max))
                .mapToObj(i -> endlessSupplier.get())
                .collect(Collectors.toList());
    }

    public static <E> E randomMemberOf(List<E> list) {
        return list.get(RANDOM.nextInt(list.size()));
    }

    public static <E, F> List<E> randomSubsetByField(List<E> list, Function<E, F> getter) {
        List<F> possibleValues = list.stream()
                .map(getter)
                .distinct()
                .collect(Collectors.toList());

        F magicValue = randomMemberOf(possibleValues);

        return list.stream()
                .filter(item -> Objects.equals(magicValue, getter.apply(item)))
                .collect(Collectors.toList());
    }

    /**
     * Produces a random LocalDateTime.
     * @return
     */
    public static LocalDateTime localDateTime() {
        LocalDateTime localDateTime = LocalDateTime.now();
        for(int i=0; i<range(5,10); i++) {
            localDateTime = bool() ?
                    localDateTime.plus(randomDuration())
                    :
                    localDateTime.minus(randomDuration());
        }
        return localDateTime;
    }

    /**
     * Produces a random Duration. Max precision is MILLIS, do not use this for LocalDates.
     * @return
     */
    public static Duration randomDuration() {
        return Duration.of(
                range(1,10),
                randomMemberOf(
                        Stream.of(
                                ChronoUnit.YEARS,
                                ChronoUnit.MONTHS,
                                ChronoUnit.DAYS,
                                ChronoUnit.HOURS,
                                ChronoUnit.MINUTES,
                                ChronoUnit.SECONDS,
                                ChronoUnit.MILLIS
                        )
                                .filter(unit -> !unit.isDurationEstimated())
                                .collect(Collectors.toList())
                        ));
    }

    public static int range(int min, int max) {
        return RANDOM.nextInt(Math.abs(max-min))+min;
    }

    public static boolean bool() {
        return RANDOM.nextBoolean();
    }

    public static BigDecimal bigDecimal() {
        return BigDecimal.valueOf(RANDOM.nextDouble());
    }

    public static String name() {
        return randomMemberOf(NAMES);
    }

    public static String biname() {
        return name()+"-"+name();
    }

    // Something human readable for debugging
    private static final List<String> NAMES = Stream.of(
        "Donut",
        "Penguin",
        "Stumpy",
        "Whicker",
        "Shadow",
        "Howard",
        "Wilshire",
        "Darling",
        "Disco",
        "Jack",
        "The-Bear",
        "Sneak",
        "The-Big-L",
        "Whisp",
        "Wheezy",
        "Crazy",
        "Goat",
        "Pirate",
        "Saucy",
        "Hambone",
        "Butcher",
        "Walla-Walla",
        "Snake",
        "Caboose",
        "Sleepy",
        "Killer",
        "Stompy",
        "Mopey",
        "Dopey",
        "Weasel",
        "Ghost",
        "Dasher",
        "Grumpy",
        "Hollywood",
        "Tooth",
        "Noodle",
        "King",
        "Cupid",
        "Prancer"
    ).collect(Collectors.toList());
}
