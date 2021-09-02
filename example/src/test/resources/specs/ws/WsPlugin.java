import java.util.Map;

public class Specs extends AbstractSpecs {
    @Override
    protected ExamExtension init() {
        return new ExamExtension(
            // all arguments are optional
            new WsPlugin(
                "http://localhost", // uri
                "",                 // context path
                8080,               // port
                Map.of(),           // additional Content-Type Configs: defines how to resolve/verify/print data of specified Content-Type
                MultiPartAware()    // Content-Type Resolver: resolves one of the supported Content-Types by contentType attribute of a command
            )
        );
    }
}