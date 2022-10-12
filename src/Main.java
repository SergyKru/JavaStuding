import JR_Syntax_Exercise.*;

public class Main {

    public static void main(String[] args) {

        CrManager m = CrManager.getInstance();

        do{

            m.configure();
            m.work();

        } while (!m.isExit());

    }
}
