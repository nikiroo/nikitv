# Fanfix

Fanfix is a small Java program that can download stories from some supported websites and render them offline.

It will convert from a (supported) URL to an .epub file for stories or a .cbz file for comics (a few other output types are also available, like Plain Text or LaTeX).

To help organize your stories, it can also work as a local library.

In this mode, you can:
- Import a story from its URL (or even another file)
- Export a story to a file (.epub or .cbz)
- Display a story from the local library

## Supported websites

Currently, the following websites are supported:
- http://FimFiction.net/: fan fictions devoted to the My Little Pony show
- http://Fanfiction.net/: fan fictions of many, many different universes, from TV shows to novels to games
- http://mangafox.me/: a well filled repository of mangas, or, as their website states: most popular manga scanlations read online for free at mangafox, as well as a close-knit community to chat and make friends
- https://e621.net/: a Furry website supporting comics, including MLP

## A note about file support

We support a few file types for local story conversion (both as input and as output):
- epub: .epub files created by this program (we do not support "all" .epub files, at least for now)
- text: local stories encoded in plain text format, with a few specific rules:
  - the title must be on the first line
  - the author (preceded by nothing, "by " or "©") must be on the second line, possibly with the publication date in parenthesis (i.e., "By Unknown (3rd October 1998)")
  - chapters must be declared with "Chapter x" or "Chapter x: NAME OF THE CHAPTER", where "x" is the chapter number
  - a description of the story must be given as chapter number 0
  - a cover image may be present with the same filename as the story, but a .png, .jpeg or .jpg extension
- info_text: contains the same information as the text format, but with a companion .info file to store some metadata (the .info file is supposed to be created by Fanfix or compatible with it)
- cbz: .cbz (collection of images) files, preferably created with Fanfix (but any .cbz file is supported, though without most of Fanfix metadata, obviously)

## Supported platforms

Any platform with at lest Java 1.6 on it should be ok.

If you have any problems to compile it with a supported Java version (1.5 won't work, but you may try to cross-compile or change the Bundle.java class from the utilities; 1.6 and 1.8 have been tested and work), please contact me.

## Usage

You can start the program in CLI mode:
- ```java -jar fanfix.jar```

__TODO__: offer a GUI mode (work in progress)

The following arguments are allowed:
- ```--import [URL]```: import the story at URL into the local library
- ```--export [id] [output_type] [target]```: export the story denoted by ID to the target file
- ```--convert [URL] [output_type] [target] (+info)```: convert the story at URL into target, and force-add the .info and cover if +info is passed
- ```--read [id] ([chapter number])```: read the given story denoted by ID from the library
- ```--read-url [URL] ([chapter number])```: convert on the fly and read the story denoted by ID, without saving it
- ```--list```: list the stories present in the library and their associated IDs
- ```--set-reader [reader type]```: set the reader type to CLI or LOCAL for this command
- ```--help```: display the available options

### Environment variables

Some environment variables are recognized by the program:
- ```LANG=en```: force the language to English (the only one for now...)
- ```CONFIG_DIR=$HOME/.fanfix```: use the given directory as a config directory (and copy the default configuration if needed)
- ```NOUTF=1```: try to fallback to non-unicode values when possible (can have an impact on the resulting files, not only on user messages)
- ```DEBUG=1```: force the DEBUG=true option of the configuration file (to show more information on errors)

## Compilation

```./configure.sh && make```

You can also import the java sources into, say, [Eclipse](https://eclipse.org/), and create a runnable JAR file from there.

### Dependant libraries (included)

- libs/nikiroo-utils-sources.jar: some shared utility functions I also use elsewhere
- [libs/unbescape-sources.jar](https://github.com/unbescape/unbescape): a nice library to escape/unescape a lot of text formats; I only use it for HTML

Nothing else but Java 1.6+.

Note that calling ```make libs``` will export the libraries into the src/ directory.

## TODO

Currently missing, but either in progress or planned:
- A GUI (work in progress)
- Some readers other than CLI (TUI, GUI)
- Check if it can work on Android
- French translation
