package be.nikiroo.fanfix.output;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import be.nikiroo.fanfix.data.MetaData;
import be.nikiroo.fanfix.data.Paragraph;
import be.nikiroo.fanfix.data.Story;
import be.nikiroo.utils.IOUtils;

class Cbz extends BasicOutput {
	private File dir;

	@Override
	public File process(Story story, File targetDir, String targetName)
			throws IOException {
		String targetNameOrig = targetName;
		targetName += getDefaultExtension(false);

		File target = new File(targetDir, targetName);

		dir = File.createTempFile("fanfic-reader-cbz-dir", ".wip");
		dir.delete();
		dir.mkdir();
		try {
			// will also save the images!
			new InfoText().process(story, dir, targetNameOrig);

			InfoCover.writeInfo(dir, targetNameOrig, story.getMeta());
			if (story.getMeta() != null && !story.getMeta().isFakeCover()) {
				InfoCover.writeCover(dir, targetNameOrig, story.getMeta());
			}

			IOUtils.writeSmallFile(dir, "version", "3.0");

			try {
				super.process(story, targetDir, targetNameOrig);
			} finally {
			}

			IOUtils.zip(dir, target, true);
		} finally {
			IOUtils.deltree(dir);
		}

		return target;
	}

	@Override
	public String getDefaultExtension(boolean readerTarget) {
		return ".cbz";
	}

	@Override
	protected void writeStoryHeader(Story story) throws IOException {
		MetaData meta = story.getMeta();

		StringBuilder builder = new StringBuilder();
		if (meta != null && meta.getResume() != null) {
			for (Paragraph para : story.getMeta().getResume()) {
				builder.append(para.getContent());
				builder.append("\n");
			}
		}

		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(new File(dir, "URL")), "UTF-8"));
		try {
			if (meta != null) {
				writer.write(meta.getUuid());
			}
		} finally {
			writer.close();
		}

		writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(new File(dir, "SUMMARY")), "UTF-8"));
		try {
			String title = "";
			if (meta != null && meta.getTitle() != null) {
				title = meta.getTitle();
			}

			writer.write(title);
			if (meta != null && meta.getAuthor() != null) {
				writer.write("\n©");
				writer.write(meta.getAuthor());
			}
			writer.write("\n\n");
			writer.write(builder.toString());
		} finally {
			writer.close();
		}
	}
}
