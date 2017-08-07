package specs.fl;

import org.concordion.api.BeforeSpecification;
import org.junit.rules.TemporaryFolder;
import specs.Exam;

import java.io.IOException;

public class Fl extends Exam {
    private static final TemporaryFolder TEMP_FOLDER = new TemporaryFolder();

    @BeforeSpecification
    public static void beforeSpec() throws IOException {
        TEMP_FOLDER.create();
    }

    public String getDir() {
        return TEMP_FOLDER.getRoot().getPath();
    }

    public boolean addFile(String name) throws IOException {
        return TEMP_FOLDER.newFile(name).exists();
    }
}
