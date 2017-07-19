package specs.exam;

import com.sberbank.pfm.test.concordion.extensions.exam.ExamExtension;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.concordion.api.extension.Extension;
import org.concordion.api.option.ConcordionOptions;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.RunWith;

@RunWith(ConcordionRunner.class)
@ConcordionOptions(declareNamespaces = {"c", "http://www.concordion.org/2007/concordion", "e", ExamExtension.NS})
public class Exam {
    @SuppressFBWarnings(value = "URF_UNREAD_FIELD", justification = "особенности подключения расширений в concordion")
    @Extension
    private final ExamExtension exam = new ExamExtension().
            dbTester("org.h2.Driver", "jdbc:h2:mem:test;INIT=CREATE SCHEMA IF NOT EXISTS SA\\;SET SCHEMA SA", "sa", "");
}