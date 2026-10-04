# API Documentation Guideline

Write Javadoc for the public interfaces of Java classes. Use its markdown features as appropriate. Use the [API reference code comments](https://developers.google.com/style/api-reference-comments) page in the *developer documentation style guide* by Google and the [Google Java style guide](https://google.github.io/styleguide/javaguide.html) as general guidance.

As per the style guides, it is not necessary to write documentation that are trivial. For example, if a getter method just returns the field's value, then a comment adds no value. The `missing` linting flag is disabled because it encourages adding trivial Javadocs. The developer is responsible for deciding which comments are warranted.

To build the documentation, use the `mvn javadoc:javadoc` command. Make sure that the documentation is generated without errors or warnings. Use the `mvn clean javadoc:javadoc` command for a full rebuild.
