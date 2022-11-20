package cinema;

import java.sql.*;

public class DbHandler {

    private static final String CON_STR = "jdbc:sqlite:cinema.s3db";
    private static Connection connection;
    public static Statement st;

    public static ResultSet rs;
    private static DbHandler instance = null;

    private static final String CREATE_FILM_TABLE =
            "CREATE TABLE if not exists films (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT,"+
                    "duration_id INTEGER, FOREIGN KEY (duration_id) REFERENCES durations (id));";

    private static final String CREATE_DURATION_TABLE =
            "CREATE TABLE if not exists durations (id INTEGER PRIMARY KEY AUTOINCREMENT, duration INTEGER)";

    private static final String CREATE_TICKETS_TABLE =
            "CREATE TABLE if not exists tickets (id INTEGER PRIMARY KEY AUTOINCREMENT, session_id INTEGER," +
                    "FOREIGN KEY (session_id) REFERENCES timetable (id));";

    private static final String CREATE_TIMETABLE_TABLE =
            "CREATE TABLE if not exists timetable (id INTEGER PRIMARY KEY AUTOINCREMENT, film_id INTEGER, " +
                    "start_time_h TEXT, start_time_m TEXT, cost_id INTEGER, FOREIGN KEY (film_id) REFERENCES films (id));" +
                    "FOREIGN KEY (cost_id) REFERENCES price (id));";

    private static final String CREATE_PRICE_TABLE =
            "CREATE TABLE if not exists price (id INTEGER PRIMARY KEY AUTOINCREMENT, cost INTEGER);";

    public static synchronized DbHandler getInstance() throws SQLException, ClassNotFoundException {
        if (instance == null)
            instance = new DbHandler();
        return instance;
    }

    private DbHandler() throws SQLException, ClassNotFoundException {
        if(connection==null) {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(CON_STR);
            createDB();
        }
    }

    public void closeConnection(){
        if (st != null){
            try {
                st.close();
            } catch (SQLException e) {
                System.out.println("Соединение закрыто");
                e.printStackTrace();
            }
        }
        if (connection != null){
            try {
                connection.close();
            } catch (SQLException e) {
                System.out.println("Соединение закрыто");
                e.printStackTrace();
            }
        }
    }


    public static void createDB() throws ClassNotFoundException, SQLException
    {
        st = connection.createStatement();

        st.execute("DROP TABLE IF EXISTS films");
        st.execute("DROP TABLE IF EXISTS durations");
        st.execute("DROP TABLE IF EXISTS tickets");
        st.execute("DROP TABLE IF EXISTS timetable");
        st.execute("DROP TABLE IF EXISTS price");

        st.execute(CREATE_DURATION_TABLE);
        System.out.println("Таблица DURATION создана.");
        st.execute(CREATE_FILM_TABLE);
        System.out.println("Таблица FILM создана.");
        st.execute(CREATE_TIMETABLE_TABLE);
        System.out.println("Таблица TIMETABLE создана.");
        st.execute(CREATE_TICKETS_TABLE);
        System.out.println("Таблица TICKETS создана.");
        st.execute(CREATE_PRICE_TABLE);
        System.out.println("Таблица PRICE создана.");
        st.close();
    }

    public void addDataToDB() throws SQLException {
        st = connection.createStatement();
        st.execute("INSERT INTO films(name, duration_id) VALUES" +
                "('Фильм 1', 1)," +
                "('Фильм 2', 1)," +
                "('Фильм 3', 3)," +
                "('Фильм 4', 2)," +
                "('Фильм 5', 3);");
        System.out.println("Фильмы добавлены");
        st.execute("INSERT INTO durations(duration) VALUES" +
                "(60)," +
                "(90)," +
                "(120);");
        System.out.println("Длительности фильмов добавлены");
        st.execute("INSERT INTO price(cost) VALUES" +
                "(100)," +
                "(300)," +
                "(500)," +
                "(700)," +
                "(1000);");
        System.out.println("Цены сформированы");
        st.execute("INSERT INTO timetable(film_id, start_time_h, start_time_m, cost_id) VALUES" +
                "(1, '11', '00', 1)," +
                "(3, '13', '00', 2)," +
                "(1, '17', '30', 2)," +
                "(5, '19', '00', 4)," +
                "(2, '22', '00', 3)," +
                "(4, '03', '00', 5)," +
                "(4, '06', '30', 1)");
        System.out.println("Расписание составлено");
        st.execute("INSERT INTO tickets(session_id) VALUES" +
                "(1)," +
                "(1)," +
                "(1)," +
                "(3)," +
                "(3)," +
                "(5)," +
                "(4)," +
                "(4)," +
                "(4)," +
                "(1)," +
                "(5)," +
                "(1)," +
                "(2)," +
                "(2)," +
                "(1);");
        System.out.println("Список билетов сформирован");
        st.close();
    }

    public void readDB() throws ClassNotFoundException, SQLException
    {
        rs = st.executeQuery("SELECT * FROM films");

        while(rs.next())
        {
            int id = rs.getInt("id");
            String  name = rs.getString("name");
            int  duration_id = rs.getInt("duration_id");
            System.out.println( "ID = " + id );
            System.out.println( "name = " + name );
            System.out.println( "duration_id = " + duration_id );
            System.out.println();
        }

        System.out.println("Таблица выведена");
    }

    public void findTimeTableError() throws SQLException {
        int end_time =0;
        int count = 0;
        rs = st.executeQuery("SELECT name, duration, start_time_h, start_time_m FROM " +
                "films LEFT JOIN durations ON films.duration_id = durations.id " +
                "LEFT JOIN timetable ON films.id = timetable.film_id ORDER BY start_time_h, start_time_m");

        System.out.println("Название фильма     " + "Длительность      " + "Время начала");

        while(rs.next())
        {   count++;
            String  name = rs.getString("name");
            int duration = rs.getInt("duration");
            String  start_time_h = rs.getString("start_time_h");
            String start_time_m = rs.getString("start_time_m");

            if (count>1){
                if ((Integer.parseInt(start_time_h)*60 + Integer.parseInt(start_time_m))<=end_time){
                    System.out.println("Ошибка расписания! Фильм "+ count + " начинается слишком рано");
                }
            }
            end_time = Integer.parseInt(start_time_h)*60 + Integer.parseInt(start_time_m) + duration;

            System.out.println( name + "             " +  duration + "                 " + start_time_h+":"+start_time_m);
            System.out.println();
        }
    }

    public void showFilmBreaks() throws SQLException {
        st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS breaks");
        st.execute("CREATE TABLE if not exists breaks (id INTEGER PRIMARY KEY AUTOINCREMENT, film_name TEXT, " +
                "start_time_h TEXT, start_time_m TEXT, duration INTEGER, " +
                "next_film_start_h TEXT, next_film_start_m TEXT, break INTEGER);");

        int end_time =0;
        int count = 0;
        String filmName = null;
        String start_time_h = null;
        String start_time_m = null;
        String next_film_start_h;
        String next_film_start_m;
        int break_film;
        int duration = 0;
        rs = st.executeQuery("SELECT name, duration, start_time_h, start_time_m FROM " +
                "films LEFT JOIN durations ON films.duration_id = durations.id " +
                "LEFT JOIN timetable ON films.id = timetable.film_id ORDER BY start_time_h, start_time_m");


        while(rs.next())
        {   count++;
            if (count==1) {
                filmName = rs.getString("name");
                duration = rs.getInt("duration");
                start_time_h = rs.getString("start_time_h");
                start_time_m = rs.getString("start_time_m");
                end_time = Integer.parseInt(start_time_h) * 60 + Integer.parseInt(start_time_m) + duration;
            }else {
                next_film_start_h = rs.getString("start_time_h");
                next_film_start_m = rs.getString("start_time_m");
                break_film = (Integer.parseInt(next_film_start_h) * 60 + Integer.parseInt(next_film_start_m)) - end_time;

                String prepIns =  "INSERT INTO breaks(film_name, start_time_h, start_time_m, duration, next_film_start_h, next_film_start_m, break) VALUES (?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement ps = connection.prepareStatement(prepIns);
                ps.setString(1, filmName);
                ps.setString(2, start_time_h);
                ps.setString(3, start_time_m);
                ps.setInt(4, duration);
                ps.setString(5, next_film_start_h);
                ps.setString(6, next_film_start_m);
                ps.setInt(7, break_film);
                ps.execute();

                filmName = rs.getString("name");
                duration = rs.getInt("duration");
                start_time_h = rs.getString("start_time_h");
                start_time_m = rs.getString("start_time_m");
                end_time = Integer.parseInt(start_time_h) * 60 + Integer.parseInt(start_time_m) + duration;

            }

        }
        rs = st.executeQuery("SELECT * FROM breaks ORDER BY break");
        while(rs.next())
        {
            System.out.println(rs.getInt(1) + "   "+ rs.getString(2) + "   " + rs.getString(3) + ":" + rs.getString(4) +
                    "     " + rs.getInt(5) + "     " + rs.getString(6) + ":" + rs.getString(7) +
                    "     " + rs.getInt(8));

        }

    }


}
