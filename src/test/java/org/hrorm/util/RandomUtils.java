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

    /**
     * Calls endlessSupplier.get between min and max times to produce that many E's.
     * @param min The minimum number of items
     * @param max The maximum number of items
     * @param endlessSupplier The supplier of items. Should be able to call this indefinitely.
     * @param <E> The type of thing returned.
     * @return Returns a random sized List of E, with between min and max number of elements.
     */
    public static <E> List<E> randomNumberOf(int min, int max, Supplier<E> endlessSupplier) {
        return IntStream.range(0, range(min, max))
                .mapToObj(i -> endlessSupplier.get())
                .collect(Collectors.toList());
    }

    /**
     * Picks a random element out of a list.
     * @param list The Dataset to select from.
     * @param <E> The Type of entity in the list, and the type of entity returned.
     * @return A random element from in list.
     */
    public static <E> E randomMemberOf(List<E> list) {
        return list.get(RANDOM.nextInt(list.size()));
    }

    /**
     * Produces a random subset of list, all containing the same value from getter::apply
     * @param list The Dataset to select from.
     * @param getter The Getter method on E to invoke.
     * @param <E> The Entity Type.
     * @param <F> The Field Type returned by getter::apply.
     * @return A sub selection of List, all with the same value provided by the field getter.
     */
    public static <E, F> List<E> randomSubsetByField(List<E> list, Function<E, F> getter) {
        F magicValue = randomDistinctFieldValue(list, getter);

        return list.stream()
                .filter(item -> Objects.equals(magicValue, getter.apply(item)))
                .collect(Collectors.toList());
    }

    /**
     * Produces a random, field value from within the distinct possibilities contained in list.
     * @param list The Dataset to select from.
     * @param getter The Getter method on E to invoke.
     * @param <E> The Entity Type.
     * @param <F> The Field Type returned by getter::apply.
     * @return A random one of the distinct field values.
     */
    public static <E, F> F randomDistinctFieldValue(List<E> list, Function<E, F> getter) {
        List<F> possibleValues = list.stream()
                .map(getter)
                .distinct()
                .collect(Collectors.toList());

        return randomMemberOf(possibleValues);
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

    /**
     * Produce an integer with a highest and lowest possible value.
     * @param min Lowest number we should expect
     * @param max Highest number we should expect.
     */
    public static int range(int min, int max) {
        return RANDOM.nextInt(Math.abs(max-min))+min;
    }

    /**
     * Random boolean value.
     */
    public static boolean bool() {
        return RANDOM.nextBoolean();
    }

    /**
     * Random BigDecimal. Based on double input.
     */
    public static BigDecimal bigDecimal() {
        return BigDecimal.valueOf(RANDOM.nextDouble());
    }

    /**
     * Select one of the statically-defined names below.
     */
    public static String name() {
        return randomMemberOf(NAMES);
    }

    /**
     * A little more unique than name, but still human-readable.
     */
    public static String biname() {
        return name()+"-"+name();
    }

    /**
     * Names to select from.
     */
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
