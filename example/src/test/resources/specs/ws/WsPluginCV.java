public class Specs extends AbstractSpecs {
    @Override
    protected ExamExtension init() {
        return new ExamExtension(
            new WsPlugin(
                8080,
                Map.of(
                    ContentType.JSON,
                    new ContentTypeConfig(new JsonResolver(), new JsonVerifier(), new JsonPrinter())
                )
            )
        );
    }
    ...
}