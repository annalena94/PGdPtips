package pgdp.stream;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import javax.print.attribute.standard.NumberUp;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Maxi Barmetler, ge36yog
 * @version 0.1.1
 * <p>
 * -----INFORMATION-----
 * - adjust the parameters in the designated areas
 * - the global constant N_TESTS will override all individual n_tests, if it is greater than 0.
 * - the global constant STATIC_SEED will be used for RANDOM, if it isn't -1.
 */
public class MbTestStream
{
	/*------------------------PARAMETERS---*/
	private static final long STATIC_SEED = -1; // -1 for current time
	private static final int N_TESTS = 0;      // 0 for individual adjustments
	/*-------------------------------------*/

	private static final Random RANDOM = new Random(STATIC_SEED == -1 ? System.currentTimeMillis() : STATIC_SEED);

	/**
	 * Random numbers, fixed size
	 *
	 * @param size - size of List
	 * @return random List
	 */
	private static <T extends Number> List<T> createRandomList(Supplier<T> creator, int size)
	{
		return Stream.generate(creator).limit(size).collect(Collectors.toList());
	}

	/**
	 * Random integers, fixed size
	 *
	 * @param size - size of List
	 * @return random List
	 */
	private static List<Integer> createRandomIntList(int size)
	{
		return createRandomList(RANDOM::nextInt, size);
	}

	/**
	 * Random integers, random size
	 *
	 * @param minSizeInclusive - minimum size of List (inclusive)
	 * @param maxSizeExclusive - maximum size of List (exclusive)
	 * @return random List
	 */
	private static List<Integer> createRandomIntList(int minSizeInclusive, int maxSizeExclusive)
	{
		return createRandomIntList(RANDOM.nextInt(maxSizeExclusive - minSizeInclusive) + minSizeInclusive);
	}

	private static enum Op
	{
		FILTER
	}

	@SuppressWarnings("unchecked") private static <IN extends Number, OUT extends Number> pgdp.stream.Stream<OUT> addOperation(
			pgdp.stream.Stream<IN> upStream, @NotNull Op op, Object operation)
	{
		switch (op)
		{
		case FILTER:
			return (pgdp.stream.Stream<OUT>) upStream.filter((Predicate<IN>) operation);

		default:
			return (pgdp.stream.Stream<OUT>) upStream;
		}
	}

	@SuppressWarnings("unchecked") private static <IN extends Number, OUT extends Number> java.util.stream.Stream<OUT> addOperation(
			java.util.stream.Stream<IN> upStream, @NotNull Op op, Object operation)
	{
		switch (op)
		{
		case FILTER:
			return (Stream<OUT>) upStream.filter((Predicate<? super IN>) operation);

		default:
			return (Stream<OUT>) upStream;
		}
	}

	private static List<DynamicTest> createTestList(Consumer<Long> tests, String name, int size)
	{
		return LongStream.range(0, size)
				.mapToObj(e -> DynamicTest.dynamicTest(name + " " + (e + 1), () -> tests.accept(e)))
				.collect(Collectors.toList());
	}

	@TestFactory List<DynamicTest> artemisTests()
	{
		return createTestList((index) -> {
			switch (index.intValue())
			{
			case 0:
				assertEquals(0, pgdp.stream.Stream.of().count(), "Example 1: Wrong count");
				break;

			case 1:
				assertEquals(List.of(), pgdp.stream.Stream.of().toCollection(ArrayList::new), "Example 2: Wrong list");
				break;

			case 2:
				assertEquals(3, pgdp.stream.Stream.of(1, 2, 3).count(), "Example 3: Wrong count");
				break;

			case 3:
				assertEquals(List.of(1, 2, 3), pgdp.stream.Stream.of(1, 2, 3).toCollection(ArrayList::new),
						"Example 4: Wrong List");
				break;

			case 4:
				assertEquals(1, pgdp.stream.Stream.of(1, 2, 3).filter(i -> i % 2 == 0).count(),
						"Example 5: Wrong count");
				break;

			case 5:
				assertEquals(Optional.of(4),
						pgdp.stream.Stream.of(1, 2, 3).map(i -> i * i).filter(i -> i % 2 == 0).findFirst(),
						"Example 6: Wrong value");
				break;

			case 6:
				assertEquals(3, pgdp.stream.Stream.of(1, 2, 3).map(i -> {
					if (i % 2 == 0)
						throw new IllegalArgumentException();
					return i;
				}).count(), "Example 7: Wrong count");
				break;

			case 7:
				assertEquals(List.of(42, 3, 42, 5), pgdp.stream.Stream.of(1, 2, 3, 4, 5).map(i -> {
					if (i % 2 == 0)
						throw new IllegalArgumentException();
					return i;
				}).filter(i -> i > 1).onErrorMap(list -> 42).toCollection(ArrayList::new), "Example 8: Wrong List");
				break;

			case 8:
				assertEquals(List.of("1", "2", "x:3", "null", "x:6"),
						pgdp.stream.Stream.of(1, 2, 3, 4, 5, 6).mapChecked(i -> {
							if (i % 3 == 0)
								throw new IOException("x:" + i);
							return i;
						}).filter(i -> i != 5).map(i -> i == 4 ? null : i).map(Object::toString)
								.onErrorMap(list -> list.get(0).getMessage()).toCollection(ArrayList::new).stream()
								.map(e -> e == null ? "null" : e).collect(Collectors.toList()),
						"Example 9: Wrong List");
				break;

			case 9:
				assertEquals(List.of(2, 1),
						pgdp.stream.Stream.of(1, 2, 3, 4, 5, 6).map(i -> i / (i - 1)).distinct().onErrorFilter()
								.toCollection(ArrayList::new), "Example 10: Wrong List");
				break;

			case 10:
				Exception was = null;
				try
				{
					pgdp.stream.Stream.of(1, 2, 3, 4, 5, 6).mapChecked(i -> {
						if (i > 10)
							throw new IOException("x:" + i);
						return i;
					}).reduce(Integer::sum);
				} catch (CheckedStreamException e)
				{
					was = e;
				}
				assertNotNull(was, "Example 11: No Exception");
				assertEquals(CheckedStreamException.class, was.getClass(), "Example 11: Wrong Exception");

				break;
			}
		}, "artemisTest", 11);
	}

	@TestFactory List<DynamicTest> countKnownSize()
	{
		/*---PARAMETERS---*/
		int nTests = 100;
		/*----------------*/

		if (N_TESTS > 0)
			nTests = N_TESTS;

		nTests += 2; // edge cases

		return createTestList((index) -> {

			List<Integer> numbers;

			switch (index.intValue())
			{
			case 0:
				numbers = List.of();
				break;

			case 1:
				numbers = List.of(1, 2, 3);
				break;

			default:
				numbers = createRandomIntList(10, 100);
			}

			long expectedCount = numbers.size();
			long actualCount = pgdp.stream.Stream.of(numbers).count();

			assertEquals(expectedCount, actualCount, "Wrong count in test " + index);

		}, "countKnownSize", nTests);
	}

	@TestFactory List<DynamicTest> countUnknownSize()
	{
		/*---PARAMETERS---*/
		int nTests = 100;
		/*----------------*/

		if (N_TESTS > 0)
			nTests = N_TESTS;

		nTests += 2; // edge cases

		return createTestList((index) -> {

			List<Integer> numbers;

			switch (index.intValue())
			{
			case 0:
				numbers = List.of();
				break;

			case 1:
				numbers = List.of(1, 2, 3);
				break;

			default:
				numbers = createRandomIntList(10, 100);
			}

			long expectedCount = numbers.size();
			long actualCount = pgdp.stream.Stream.of(numbers.stream()).count();

			assertEquals(expectedCount, actualCount, "Wrong count in test " + index);

		}, "countUnknownSize", nTests);
	}

	@TestFactory List<DynamicTest> findFirst()
	{
		/*---PARAMETERS---*/
		int nTests = 100;
		/*----------------*/

		if (N_TESTS > 0)
			nTests = N_TESTS;

		nTests += 2; // edge cases

		return createTestList((index) -> {

			List<Integer> numbers;

			switch (index.intValue())
			{
			case 0:
				numbers = List.of();
				break;

			case 1:
				numbers = List.of(1, 2, 3);
				break;

			default:
				numbers = createRandomIntList(10, 100);
			}

			Optional<Integer> expectedValue = numbers.isEmpty() ? Optional.empty() : Optional.of(numbers.get(0));
			Optional<Integer> actualValue = pgdp.stream.Stream.of(numbers).findFirst();

			assertEquals(expectedValue, actualValue, "Wrong value in test " + index);

		}, "findFirst", nTests);
	}

	@TestFactory List<DynamicTest> filter()
	{
		/*---PARAMETERS---*/
		int nTests = 10;
		int nFilters = 2;
		/*----------------*/

		if (N_TESTS > 0)
			nTests = N_TESTS;

		nTests += 2; // edge cases

		return createTestList((index) -> {

			List<Integer> numbers;

			numbers = createRandomIntList(10, 100);

			java.util.stream.Stream<Integer> realStream = numbers.stream();
			pgdp.stream.Stream<Integer> pgdpStream = pgdp.stream.Stream.of(numbers);

			Predicate<Integer> pred;
			switch (index.intValue())
			{
			case 0:
				realStream = addOperation(realStream, Op.FILTER, pred = e -> true);
				pgdpStream = addOperation(pgdpStream, Op.FILTER, pred = e -> true);
				break;

			case 1:
				realStream = addOperation(realStream, Op.FILTER, pred = e -> false);
				pgdpStream = addOperation(pgdpStream, Op.FILTER, pred = e -> false);
				break;

			default:
				for (int i = 0; i < nFilters; ++i)
				{
					final int mod = RANDOM.nextInt(63) + 1;
					pred = (num) -> num % mod != 0;

					realStream = addOperation(realStream, Op.FILTER, pred);
					pgdpStream = addOperation(pgdpStream, Op.FILTER, pred);
				}
			}

			List<Integer> expectedList = realStream.collect(Collectors.toList());
			List<Integer> actualList = (List<Integer>) pgdpStream.toCollection(ArrayList::new);

			assertEquals(expectedList, actualList, "Wrong List in test " + index);

		}, "filter", nTests);
	}
}
