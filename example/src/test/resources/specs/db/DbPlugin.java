public class Specs extends AbstractSpecs {
    @Override
    protected ExamExtension init() {
        return new ExamExtension(
            new DbPlugin(
                "org.postgresql.Driver",                     // driver class
                "jdbc:postgresql://localhost:5432/postgres", // jdbc url
                "postgres",                                  // user
                "postgres"                                   // password
            )
        );
    }
}