# Movie Indexer

This is a simple java app to create lists of movies, series, cartoons and anything else from IMDB.

[[https://cloud.githubusercontent.com/assets/9117323/17272278/5bef6a18-568a-11e6-8528-ebea143b86a5.png|alt=octocat]]

## Examples

Here is an example of the Add menu. Just type the IMDB ID, which you can find it in the URL, and press **Search**. For example, for Fight Club, [http://www.imdb.com/title/tt0137523/](http://www.imdb.com/title/tt0137523/), the ID is **tt0137523**. You can update information and set an ordering title (if you want movies to be organized by something other than alphabetically).

[[https://cloud.githubusercontent.com/assets/9117323/17272277/5bee7932-568a-11e6-8b88-40a959a64431.png|alt=octocat]]

You can also search for terms, actors, names, in any of the lists you created.

[[https://cloud.githubusercontent.com/assets/9117323/17272279/5bf0131e-568a-11e6-8cd8-cc34b3694c02.png|alt=octocat]]

## Running

Since this is Java, all you have to do is to have Java (JRE 8u40+) installed. To run, just open the `dist/MovieIndexer.jar` with Java, or run on the command line `java -jar MovieIndexer.jar`.

## Compiling and Building

If you have NetBeans, you should be able to load the project and just import the `lib/JSON.jar` library. Otherwise, just open the sources with any editor, include that file and compile with JDK 8u40 (or more recent).