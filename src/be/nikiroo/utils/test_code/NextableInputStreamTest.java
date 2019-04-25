package be.nikiroo.utils.test_code;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import be.nikiroo.utils.IOUtils;
import be.nikiroo.utils.NextableInputStream;
import be.nikiroo.utils.NextableInputStreamStep;
import be.nikiroo.utils.test.TestCase;
import be.nikiroo.utils.test.TestLauncher;

public class NextableInputStreamTest extends TestLauncher {
	public NextableInputStreamTest(String[] args) {
		super("NextableInputStream test", args);

		addTest(new TestCase("Simple byte array reading") {
			@Override
			public void test() throws Exception {
				byte[] expected = new byte[] { 42, 12, 0, 127 };
				NextableInputStream in = new NextableInputStream(
						new ByteArrayInputStream(expected), null);
				in.next();
				byte[] actual = IOUtils.toByteArray(in);

				assertEquals(
						"The resulting array has not the same number of items",
						expected.length, actual.length);
				for (int i = 0; i < expected.length; i++) {
					assertEquals("Item " + i + " (0-based) is not the same",
							expected[i], actual[i]);
				}
			}
		});

		addTest(new TestCase("Stop at 12") {
			@Override
			public void test() throws Exception {
				byte[] expected = new byte[] { 42, 12, 0, 127 };
				NextableInputStream in = new NextableInputStream(
						new ByteArrayInputStream(expected),
						new NextableInputStreamStep(12));

				checkNext(this, "FIRST", in, new byte[] { 42 });
			}
		});

		addTest(new TestCase("Stop at 12, resume, stop again, resume") {
			@Override
			public void test() throws Exception {
				byte[] data = new byte[] { 42, 12, 0, 127, 12, 51, 11, 12 };
				NextableInputStream in = new NextableInputStream(
						new ByteArrayInputStream(data),
						new NextableInputStreamStep(12));

				checkNext(this, "FIRST", in, new byte[] { 42 });
				checkNext(this, "SECOND", in, new byte[] { 0, 127 });
				checkNext(this, "THIRD", in, new byte[] { 51, 11 });
			}
		});

		addTest(new TestCase("Encapsulation") {
			@Override
			public void test() throws Exception {
				byte[] data = new byte[] { 42, 12, 0, 4, 127, 12, 5 };
				NextableInputStream in4 = new NextableInputStream(
						new ByteArrayInputStream(data),
						new NextableInputStreamStep(4));
				NextableInputStream subIn12 = new NextableInputStream(in4,
						new NextableInputStreamStep(12));

				in4.next();
				checkNext(this, "SUB FIRST", subIn12, new byte[] { 42 });
				checkNext(this, "SUB SECOND", subIn12, new byte[] { 0 });

				assertEquals("The subIn still has some data", false,
						subIn12.next());

				checkNext(this, "MAIN LAST", in4, new byte[] { 127, 12, 5 });
			}
		});

		addTest(new TestCase("UTF-8 text lines test") {
			@Override
			public void test() throws Exception {
				String ln1 = "Ligne première";
				String ln2 = "Ligne la deuxième du nom";
				byte[] data = (ln1 + "\n" + ln2).getBytes("UTF-8");
				NextableInputStream in = new NextableInputStream(
						new ByteArrayInputStream(data),
						new NextableInputStreamStep('\n'));

				checkNext(this, "FIRST", in, ln1.getBytes("UTF-8"));
				checkNext(this, "SECOND", in, ln2.getBytes("UTF-8"));
			}
		});

		addTest(new TestCase("nextAll()") {
			@Override
			public void test() throws Exception {
				byte[] data = new byte[] { 42, 12, 0, 127, 12, 51, 11, 12 };
				NextableInputStream in = new NextableInputStream(
						new ByteArrayInputStream(data),
						new NextableInputStreamStep(12));

				checkNext(this, "FIRST", in, new byte[] { 42 });
				checkNextAll(this, "REST", in,
						new byte[] { 0, 127, 12, 51, 11, 12 });
				assertEquals("The stream still has some data", false,
						in.next());
			}
		});

		addTest(new TestCase("getBytesRead()") {
			@Override
			public void test() throws Exception {
				byte[] data = new byte[] { 42, 12, 0, 127, 12, 51, 11, 12 };
				NextableInputStream in = new NextableInputStream(
						new ByteArrayInputStream(data),
						new NextableInputStreamStep(12));

				in.nextAll();
				IOUtils.toByteArray(in);

				assertEquals("The number of bytes read is not correct",
						data.length, in.getBytesRead());
			}
		});

		addTest(new TestCase("bytes array input") {
			@Override
			public void test() throws Exception {
				byte[] data = new byte[] { 42, 12, 0, 127, 12, 51, 11, 12 };
				NextableInputStream in = new NextableInputStream(data,
						new NextableInputStreamStep(12));

				checkNext(this, "FIRST", in, new byte[] { 42 });
				checkNext(this, "SECOND", in, new byte[] { 0, 127 });
				checkNext(this, "THIRD", in, new byte[] { 51, 11 });
			}
		});

		addTest(new TestCase("Skip data") {
			@Override
			public void test() throws Exception {
				byte[] data = new byte[] { 42, 12, 0, 127, 12, 51, 11, 12 };
				NextableInputStream in = new NextableInputStream(data, null);
				in.next();

				in.skip(4);
				checkArrays(this, "ONLY", in, new byte[] { 12, 51, 11, 12 });
			}
		});

		addTest(new TestCase("Starts with") {
			@Override
			public void test() throws Exception {
				byte[] data = new byte[] { 42, 12, 0, 127, 12, 51, 11, 12 };
				NextableInputStream in = new NextableInputStream(data, null);
				in.next();

				// yes
				assertEquals("It actually starts with that", true,
						in.startsWith(new byte[] { 42 }));
				assertEquals("It actually starts with that", true,
						in.startsWith(new byte[] { 42, 12 }));
				assertEquals("It actually is the same array", true,
						in.startsWith(data));

				// no
				assertEquals("It actually does not start with that", false,
						in.startsWith(new byte[] { 12 }));
				assertEquals("It actually does not start with that", false,
						in.startsWith(
								new byte[] { 42, 12, 0, 127, 12, 51, 11, 11 }));

				// too big
				try {
					in.startsWith(
							new byte[] { 42, 12, 0, 127, 12, 51, 11, 12, 0 });
					fail("Searching a prefix bigger than the array should throw an IOException");
				} catch (IOException e) {
				}
			}
		});

		addTest(new TestCase("Starts with strings") {
			@Override
			public void test() throws Exception {
				String text = "Fanfan et Toto vont à la mer";
				byte[] data = text.getBytes("UTF-8");
				NextableInputStream in = new NextableInputStream(data, null);
				in.next();

				// yes
				assertEquals("It actually starts with that", true,
						in.startsWiths("F"));
				assertEquals("It actually starts with that", true,
						in.startsWiths("Fanfan et"));
				assertEquals("It actually is the same text", true,
						in.startsWiths(text));

				// no
				assertEquals("It actually does not start with that", false,
						in.startsWiths("Toto"));
				assertEquals("It actually does not start with that", false,
						in.startsWiths("Fanfan et Toto vont à la mee"));

				// too big
				try {
					in.startsWiths("Fanfan et Toto vont à la mer.");
					fail("Searching a prefix bigger than the array should throw an IOException");
				} catch (IOException e) {
				}
			}
		});
	}

	static void checkNext(TestCase test, String prefix, NextableInputStream in,
			byte[] expected) throws Exception {
		test.assertEquals("Cannot get " + prefix + " entry", true, in.next());
		checkArrays(test, prefix, in, expected);
	}

	static void checkNextAll(TestCase test, String prefix,
			NextableInputStream in, byte[] expected) throws Exception {
		test.assertEquals("Cannot get " + prefix + " entries", true,
				in.nextAll());
		checkArrays(test, prefix, in, expected);
	}

	static void checkArrays(TestCase test, String prefix,
			NextableInputStream in, byte[] expected) throws Exception {
		byte[] actual = IOUtils.toByteArray(in);
		test.assertEquals("The " + prefix
				+ " resulting array has not the correct number of items",
				expected.length, actual.length);
		for (int i = 0; i < actual.length; i++) {
			test.assertEquals("Item " + i + " (0-based) is not the same",
					expected[i], actual[i]);
		}
	}
}
