package at.kaindorf.taschengeldverwaltung.database;


import at.kaindorf.taschengeldverwaltung.pojos.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Access {

    public static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DTF_date = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static Access theInstance;

    private static Database dbInstance;

    private Access() {
        dbInstance = Database.getTheInstance();
    }

    public static Access getTheInstance(){
        if (theInstance == null){
            theInstance = new Access();
        }
        return theInstance;
    }

    public List<BalanceVillagerPerson> getAllVillagers() throws SQLException {
        Statement statement = dbInstance.getStatement();
        List<BalanceVillagerPerson> villagerPeople = new ArrayList<>();

        String sqlString = "SELECT v.villager_id, p.firstname, p.lastname, p.title_before, p.title_after, p.salutation_id, s.salutation_text, \n" +
                "\t\tv.short_sign, v.date_of_birth, v.date_of_exit, v.note, SUM(amount) AS balance\n" +
                "FROM villager v\n" +
                "\tINNER JOIN person p ON v.villager_id = p.person_id\n" +
                "\tINNER JOIN salutation s ON p.salutation_id = s.salutation_id\n" +
                "\tINNER JOIN booking b ON b.villager_id = p.person_id\n" +
                "GROUP BY v.villager_id, p.firstname, p.lastname, p.title_before, p.title_after, p.salutation_id, s.salutation_text, \n" +
                "\t\tv.short_sign, v.date_of_birth, v.date_of_exit, v.note\n" +
                "ORDER BY v.villager_id;";

        ResultSet results = statement.executeQuery(sqlString);

        while (results.next()){
            villagerPeople.add(new BalanceVillagerPerson(results.getLong("villager_id"),
                    results.getString("firstname"),
                    results.getString("lastname"),
                    results.getString("title_before"),
                    results.getString("title_after"),
                    new Salutation(results.getLong("salutation_id"), results.getString("salutation_text")),
                    results.getString("short_sign"),
                    results.getDate("date_of_birth").toLocalDate(),
                    results.getDate("date_of_exit").toLocalDate(),
                    results.getString("note"),
                    results.getDouble("balance")));
        }
        dbInstance.releaseStatement(statement);
        return villagerPeople;
    }
    public TrustedPerson getPersonOfTrustByVillagerId(Long id) throws SQLException {
        Statement statement = dbInstance.getStatement();

        String sqlString = "SELECT *\n" +
                "FROM person p \n" +
                "\tINNER JOIN trusted_person tp ON p.person_id = tp.trusted_person_id\n" +
                "\tINNER JOIN transmission_method tm ON tp.transmission_method = tm.transmission_method_id\n" +
                "\tINNER JOIN relation re ON tp.relation = re.relation_id\n" +
                "\tINNER JOIN salutation s ON p.salutation_id = s.salutation_id\n" +
                "WHERE p.person_id = (\n" +
                "\tSELECT trusted_person\n" +
                "\tFROM person pp INNER JOIN villager v ON pp.person_id = v.villager_id\n" +
                "\tWHERE person_id = 455\n" +
                ");";

        ResultSet results = statement.executeQuery(sqlString);

        dbInstance.releaseStatement(statement);
        results.next();


           return new TrustedPerson(results.getLong("person_id"),
                    results.getString("firstname"),
                    results.getString("lastname"),
                    results.getString("title_before"),
                    results.getString("title_after"),
                    new Salutation(results.getLong("salutation_id"), results.getString(21)),
                    results.getString("email"),
                    results.getString("tel_nr"),
                    results.getString("town"),
                    results.getString("zip_code"),
                    results.getString("street"),
                    results.getString("house_nr"),
                    new Relation(
                            results.getInt("relation_id"),
                            results.getString(19)
                    ),
                    new TransmissionMethod(
                            results.getLong("transmission_method_id"),
                            results.getString(17)
                    ));
    }

    public VillagerPerson getVillagerById(Long id) throws SQLException {
        Statement statement = dbInstance.getStatement();

//        String sqlString = "SELECT *\n" +
//                "FROM \"person\" p INNER JOIN \"villager\" vp ON p.\"person_id\" = vp.\"villager_id\"\n" +
//                "                INNER JOIN \"transmission_method\" v ON vp.\"transmission_method\" = v.\"transmission_method_id\"\n" +
//                "                INNER JOIN \"salutation\" a ON p.\"salutation_id\" = a.\"salutation_id\"" +
//                "WHERE p.\"person_id\" = "+ id+ ";\n";

        String sqlString = "SELECT * FROM person p INNER JOIN villager vp ON p.person_id = vp.villager_id\n" +
                "INNER JOIN salutation a ON p.salutation_id = a.salutation_id\n" +
                "WHERE p.person_id = "+ id+ ";";

        ResultSet results = statement.executeQuery(sqlString);

        dbInstance.releaseStatement(statement);
        results.next();


        return new VillagerPerson(results.getLong("person_id"),
                results.getString("firstname"),
                results.getString("lastname"),
                results.getString("title_before"),
                results.getString("title_after"),
                new Salutation(results.getLong("salutation_id"), results.getString("salutation_text")),
                results.getString("short_sign"),
                results.getDate("date_of_birth").toLocalDate(),
                results.getDate("date_of_exit").toLocalDate(),
                results.getString("note"),
                new Person(results.getLong("person_id"))
                );
    }

    /*ToDo: */
    public Booking getBookingById(){

        return null;
    }


    public void updateVillager(VillagerPerson vp) throws SQLException {
        Statement statement = dbInstance.getStatement();

        String personSQL = String.format("UPDATE person\n" +
                "\tSET person_id=%d, firstname='%s', lastname='%s', salutation_id=%d, title_before='%s', title_after='%s'\n" +
                "\tWHERE person_id = %d;" ,vp.getId(), vp.getFirstName(), vp.getLastName(), vp.getSalutation().getId(),
                vp.getTitleBefore(), vp.getTitleAfter(), vp.getId());

        System.out.println(personSQL);

        String villagerSQL = String.format("UPDATE villager\n" +
                "\tSET villager_id=%d, short_sign='%s', date_of_birth='%s', date_of_exit='%s', note='%s', trusted_person=%d\n" +
                "\tWHERE villager_id = %d;", vp.getId(), vp.getShortSign(), vp.getDateOfBirth().format(DTF_date), vp.getDateOfExit().format(DTF_date), vp.getNote(), vp.getTrustedPerson().getId(), vp.getId());

        System.out.println(villagerSQL);
        statement.execute(villagerSQL);
        statement.execute(personSQL);

        dbInstance.releaseStatement(statement);
    }

    public List<Booking> getVillagerBookingHistory(Long personId) throws SQLException {
        Statement statement = dbInstance.getStatement();
        List<Booking> bookings = new ArrayList<>();

        String sqlString = "SELECT *\n" +
                "FROM \"booking\" b\n" +
                "    INNER JOIN \"tgv_user\" usr ON b.\"user_id\" = usr.\"user_id\"\n" +
                "    INNER JOIN \"purpose\" z ON b.\"purpose_id\" = z.\"purpose_id\"" +
                "WHERE \"villager_id\" = "+ personId+ ";";

        ResultSet results = statement.executeQuery(sqlString);

        while (results.next()){
            bookings.add(
                    new Booking(results.getLong("villager_id"),
                            results.getDate("date_of_booking").toLocalDate()
                            .atTime(results.getTime("date_of_booking").toLocalTime()),
                            results.getString("username"),
                            results.getFloat("amount"),
                            results.getLong("receipt_nr"),
                            results.getString("note"),
                            new Purpose(
                                    results.getLong("purpose_id"),
                                    results.getString("text"),
                                    results.getShort("multiplier"),
                                    results.getBoolean("status"))
                            )
            );
        }
        dbInstance.releaseStatement(statement);
        return bookings;
    }

    public List<Booking> getAllVillagersBookingHistory(String sortedBy) throws SQLException {
//        sortedBy specifies the column to be sorted

        Statement statement = dbInstance.getStatement();
        List<Booking> bookings = new ArrayList<>();

        String sqlString = "SELECT *\n" +
                "FROM \"booking\" b\n" +
                "    INNER JOIN \"user\" usr ON b.\"user_id\" = usr.\"user_id\"\n" +
                "    INNER JOIN \"purpose\" z ON b.\"purpose_id\" = z.\"purpose_id\"";

        ResultSet results = statement.executeQuery(sqlString);

        while (results.next()){
            bookings.add(
                    new Booking(results.getLong("villager_id"),
                            results.getDate("date_of_booking").toLocalDate()
                            .atTime(results.getTime("date_of_booking").toLocalTime()),
                            results.getString("username"),
                            results.getFloat("amount"),
                            results.getLong("receipt_nr"),
                            results.getString("note"),
                            new Purpose(
                                    results.getLong("purpose_id"),
                                    results.getString("text"),
                                    results.getShort("multiplier"),
                                    results.getBoolean("status"))
                    )
            );
        }
        dbInstance.releaseStatement(statement);

        switch (sortedBy.toLowerCase()){
            case "date" -> bookings.stream().sorted(Comparator.comparing(Booking::getDateTime));
            case "user" -> bookings.stream().sorted(Comparator.comparing(Booking::getUsername));
            default -> bookings.stream().sorted(Comparator.comparing(Booking::getReceiptNumber));
        }
        return bookings;
    }

    private Long getIdOfUser(String username) throws SQLException {
        Statement statement = dbInstance.getStatement();
        String sqlString = "SELECT \"user_id\"\n" +
                "FROM \"tgv_user\"\n" +
                "WHERE \"username\" = '"+ username+"';";

        ResultSet results = statement.executeQuery(sqlString);
        results.next();
        dbInstance.releaseStatement(statement);
        return results.getLong("user_id");
    }

    public void insertFastBooking(Long personId, Booking booking) throws SQLException {
        LocalDateTime dateOfBooking = booking.getDateTime();

        String sqlString = String.format("INSERT INTO public.\"booking\"(\n" +
                "    \"villager_id\", \"date_of_booking\", \"purpose_id\", \"amount\", \"receipt_nr\", \"note\", \"user_id\")\n" +
                "VALUES (%d, '%s', %d, %f, %d, '%s', %s);", personId, dateOfBooking.format(DTF),
                booking.getPurpose().getId(),
                booking.getValue(),
                booking.getReceiptNumber(),
                booking.getNote(),
                getIdOfUser(booking.getUsername())
                );
        Statement statement = dbInstance.getStatement();
        statement.execute(sqlString);

        dbInstance.releaseStatement(statement);
    }

    public BalanceOverview getBalanceList() throws SQLException {

        String sqlString = "SELECT *\n" +
                "FROM \"booking\" b\n" +
                "    INNER JOIN \"person\" be ON b.\"villager_id\" = be.\"person_id\"\n" +
                "    INNER JOIN \"purpose\" z ON b.\"purpose_id\" = z.\"purpose_id\";";

        Statement statement = dbInstance.getStatement();

        ResultSet results = statement.executeQuery(sqlString);
        Map<Person, Double> balanceMap = new HashMap<>();


        while (results.next()){
            Person person = new Person(
                    results.getLong("villager_id"),
                    results.getString("firstname"),
                    results.getString("lastname"));

            if (balanceMap.containsKey(person)){
                Double balance = balanceMap.get(person);
                balance += (results.getDouble("amount") * results.getShort("multiplier"));
                balanceMap.put(person,balance);
                continue;
            }

            balanceMap.put(person, (results.getDouble("amount") * results.getShort("multiplier")));
        }
        dbInstance.releaseStatement(statement);

        List<Balance> balanceList = new ArrayList<>();

        for (Person person : balanceMap.keySet()){
            balanceList.add(new Balance(person.getId(), person.getFirstName(),
                    person.getLastName(), balanceMap.get(person)));
        }

        return new BalanceOverview(getOverallBalance(balanceList), balanceList);
    }

    private Double getOverallBalance(List<Balance> balanceList) {
        return balanceList.stream()
                .mapToDouble(b -> b.getBalance())
                .sum();
    }

    public BalanceOverview getAccountingJournal(Long personId) throws SQLException {
        String sqlString = "SELECT *\n" +
                "FROM \"booking\" b\n" +
                "    INNER JOIN \"person\" be ON b.\"villager_id\" = be.\"person_id\"\n" +
                "    INNER JOIN \"purpose\" z ON b.\"purpose_id\" = z.\"purpose_id\";";

        Statement statement = dbInstance.getStatement();

        ResultSet results = statement.executeQuery(sqlString);
        List<Balance> balanceList = new ArrayList<>();
        BalanceOverview balanceOverview = new BalanceOverview(0.0,0.0,0.0);

        while (results.next()){
            Double value = (results.getDouble("amount") * results.getShort("multiplier"));

            balanceList.add(new Balance(
                        results.getLong("villager_id"),
                        results.getString("firstname"),
                        results.getString("lastname"),
                        value,
                        results.getLong("receipt_nr"),
                        results.getDate("date_of_booking").toLocalDate(),
                        results.getString("text")
            ));

            balanceOverview.setSum(balanceOverview.getSum()+value);
            if (value<0){
                balanceOverview.setExpenses(balanceOverview.getExpenses()+value);
                continue;
            }
            balanceOverview.setIncome(balanceOverview.getIncome()+value);
        }
        dbInstance.releaseStatement(statement);
        if (personId != null){
            balanceList = balanceList.stream()
                    .filter(balance -> balance.getVillagerId().equals(personId))
                    .collect(Collectors.toList());
        }
        balanceOverview.setBalanceList(balanceList);
        return balanceOverview;
    }

    public List<Salutation> getAllSalutations() throws SQLException {
        String sqlString = "SELECT * FROM salutation;";

        Statement statement = dbInstance.getStatement();

        ResultSet results = statement.executeQuery(sqlString);
        List<Salutation> salutations = new ArrayList<>();
        while (results.next()){
            salutations.add(new Salutation(results.getLong("salutation_id"),
                    results.getString("salutation_text")));
        }
        dbInstance.releaseStatement(statement);
        return salutations;
    }

    public List<Relation> getAllRelations() throws SQLException {
        String sqlString = "SELECT * FROM relation;";

        Statement statement = dbInstance.getStatement();

        ResultSet results = statement.executeQuery(sqlString);
        List<Relation> relations = new ArrayList<>();
        while (results.next()){
            relations.add(new Relation(results.getLong("relation_id"),
                    results.getString("relation")));
        }
        dbInstance.releaseStatement(statement);
        return relations;
    }

    public List<Purpose> getAllPurposes() throws SQLException {
        String sqlString = "SELECT * FROM purpose;";

        Statement statement = dbInstance.getStatement();

        ResultSet results = statement.executeQuery(sqlString);
        List<Purpose> purposes = new ArrayList<>();
        while (results.next()){
            purposes.add(new Purpose(results.getLong("purpose_id"),
                    results.getString("text"),
                    results.getShort("multiplier"),
                    results.getBoolean("status")));
        }
        dbInstance.releaseStatement(statement);
        return purposes;
    }

    public List<TransmissionMethod> getAllTransmissionMethods() throws SQLException {
        String sqlString = "SELECT * FROM transmission_method;";

        Statement statement = dbInstance.getStatement();

        if (statement == null) {
            statement = dbInstance.getStatement();
        }

        ResultSet results = statement.executeQuery(sqlString);
        List<TransmissionMethod> methods = new ArrayList<>();
        while (results.next()){
            methods.add(new TransmissionMethod(results.getLong("transmission_method_id"),
                    results.getString("method")));
        }
        dbInstance.releaseStatement(statement);
        return methods;
    }

    public List<ShortVillager> fastBookingSearch(String pattern) throws SQLException {
        String sqlString = "SELECT *\n" +
                "FROM villager v\n" +
                "\tINNER JOIN person p ON v.villager_id = p.person_id\n" +
                "WHERE LOWER(v.short_sign) LIKE LOWER('%"+pattern+"%');";

        Statement statement = dbInstance.getStatement();

        ResultSet results = statement.executeQuery(sqlString);
        List<ShortVillager> shorties = new ArrayList<>();
        while (results.next()){
            shorties.add(new ShortVillager(results.getLong("villager_id"),
                    results.getString("short_sign")));
        }
        dbInstance.releaseStatement(statement);
        return shorties;
    }

    public void updateTrustedPerson(TrustedPerson tp)  throws SQLException{
        Statement statement = dbInstance.getStatement();

        String personSQL = String.format("UPDATE person\n" +
                        "\tSET person_id=%d, firstname='%s', lastname='%s', salutation_id=%d, title_before='%s', title_after='%s'\n" +
                        "\tWHERE person_id = %d;" ,tp.getId(), tp.getFirstName(), tp.getLastName(), tp.getSalutation().getId(),
                tp.getTitleBefore(), tp.getTitleAfter(), tp.getId());

        System.out.println(personSQL);

        String trustedPersonSQL = String.format("UPDATE trusted_person\n" +
                "\tSET trusted_person_id=%d, tel_nr='%s', transmission_method='%d', town='%s', zip_code='%s', street='%s', house_nr='%s', relation=%d, email='%s'\n" +
                "\tWHERE trusted_person_id=%d;", tp.getId(), tp.getTelNr(), tp.getMethod().getId(), tp.getTown(), tp.getZipCode(), tp.getStreet(), tp.getHouseNr(), tp.getRelation().getId(), tp.getEmail(), tp.getId());

        System.out.println(trustedPersonSQL);

        statement.execute(trustedPersonSQL);
        statement.execute(personSQL);

        dbInstance.releaseStatement(statement);
    }


//    public static void main(String[] args) {
//        try {
//
//            System.out.println(getTheInstance().getAllSalutations());
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }






//    LocalDateTime ld = LocalDateTime.now();
//        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//        System.out.println(ld.format(dtf));
}
