package cinema;

import java.sql.SQLException;

public class CinemaApp {
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        DbHandler dbHandler = DbHandler.getInstance();
        dbHandler.addDataToDB();
        dbHandler.readDB();
        dbHandler.findTimeTableError();
        dbHandler.showFilmBreaks();
        dbHandler.closeConnection();
    }
}
