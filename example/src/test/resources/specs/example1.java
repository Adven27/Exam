@RunWith(ConcordionRunner.class)
@ConcordionOptions(declareNamespaces = {"c", "http://www.concordion.org/2007/concordion", "e", ExamExtension.NS})
public class Specs {
    @Extension
    private final ExamExtension exam = new ExamExtension(
        new WsPlugin("/app", 8080),
        new DbPlugin("org.postgresql.Driver", "jdbc:postgresql://localhost:5432/postgres", "postgres", "postgres")
    );
}