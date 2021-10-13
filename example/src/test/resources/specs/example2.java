public class Specs extends AbstractSpecs {
    @Override
    protected ExamExtension init() {
        return new ExamExtension(
            new WsPlugin("/app", 8080),
            new DbPlugin("org.postgresql.Driver", "jdbc:postgresql://localhost:5432/postgres", "postgres", "postgres")
        );
    }
...
    /*  Methods are listed in order of execution: */

    @Override
    protected void beforeSetUp() {
        // Optional: Run some actions BEFORE Exam set up
    }

    @Override
    protected void beforeSutStart() {
        // Optional: Run some actions AFTER Exam set up and BEFORE start of a System Under Test
    }

    @Override
    protected void startSut() {
        // Start System Under Test before specs suite if needed
    }

    @Override
    protected void stopSut() {
        // Stop System Under Test after specs suite if needed
    }

    @Override
    protected void afterSutStop() {
        // Optional: Run some actions AFTER stop of a System Under Test and BEFORE Exam tear down
    }

    @Override
    protected void afterTearDown() {
        // Optional: Run some actions AFTER Exam tear down
    }
}