package ru.spliterash.warehouse.database;

import lombok.Getter;
import org.intellij.lang.annotations.Language;
import ru.spliterash.warehouse.datamodel.*;
import ru.spliterash.warehouse.other.Utils;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class Base {

    protected abstract Connection getConnection() throws SQLException;

    @Getter
    private final List<ContactTypeDataModel> contactTypes = new ArrayList<>(2);

    protected final void load() {
        try (Connection connection = getConnection()) {
            try (Statement statement = connection.createStatement()) {
                ResultSet set = statement.executeQuery("SELECT id,name from contact_type");
                while (set.next()) {
                    contactTypes.add(new ContactTypeDataModel(set.getInt("id"), set.getString("name")));
                }

            }
        } catch (SQLException throwables) {
            Utils.showInternalError(throwables);
        }
    }

    public void deleteCargo(int id) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM cargo where id = ?")) {
                statement.setInt(1, id);
                statement.executeUpdate();
            }
        } catch (SQLException throwables) {
            Utils.showInternalError(throwables);
        }
    }

    public List<WarehouseDataModel> getWarehouses() {
        List<WarehouseDataModel> list = new ArrayList<>();
        try (Connection connection = getConnection()) {
            try (Statement statement = connection.createStatement()) {
                ResultSet result = statement.executeQuery("SELECT id, address, area FROM warehouse");
                while (result.next()) {
                    WarehouseDataModel model = new WarehouseDataModel(
                            result.getInt("id"),
                            result.getString("address"),
                            result.getInt("area")
                    );
                    list.add(model);
                }
            }
        } catch (SQLException throwables) {
            Utils.showInternalError(throwables);
        }
        return list;
    }

    public int getWarehouseOccupied(int id) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT sum(area)\n" +
                    "from cargo\n" +
                    "where warehouse_id = ?\n" +
                    "group by warehouse_id")) {
                statement.setInt(1, id);
                ResultSet result = statement.executeQuery();
                if (result.next()) {
                    return result.getInt(1);
                } else
                    return 0;
            }
        } catch (SQLException throwables) {
            Utils.showInternalError(throwables);
            return 0;
        }
    }

    public ClientDataModel getClient(int id) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT id, first_name, last_name, middle_name from client where id = ?")) {
                statement.setInt(1, id);
                ResultSet result = statement.executeQuery();
                if (result.next()) {
                    return new ClientDataModel(
                            id,
                            result.getString("first_name"),
                            result.getString("last_name"),
                            result.getString("middle_name")
                    );
                } else
                    return null;
            }
        } catch (SQLException throwables) {
            Utils.showInternalError(throwables);
            return null;
        }
    }

    public ContactTypeDataModel getContactTypeByName(String name) {
        return contactTypes
                .stream()
                .filter(t -> t.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    private List<CargoModelData> getCargo(@Language("SQL") String query, Object... args) {
        List<CargoModelData> list = new ArrayList<>();
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                for (int i = 0; i < args.length; i++) {
                    statement.setObject(i + 1, args[i]);
                }
                ResultSet result = statement.executeQuery();
                while (result.next()) {
                    CargoModelData data = new CargoModelData(
                            result.getInt("id"),
                            result.getInt("weight"),
                            result.getInt("area"),
                            new WarehouseDataModel(
                                    result.getInt("warehouse_id"),
                                    result.getString("warehouse_address"),
                                    result.getInt("warehouse_area")
                            ),
                            getClient(result.getInt("client_id"))
                    );
                    list.add(data);
                }
            }
        } catch (SQLException throwables) {
            Utils.showInternalError(throwables);
        }
        return list;
    }

    public List<CargoModelData> getWarehouseCargo(int id) {
        return getCargo("SELECT c.id,\n" +
                "       weight,\n" +
                "       " +
                "c.area,\n" +
                "       client_id,\n" +
                "       w.id     'warehouse_id',\n" +
                "       w.address 'warehouse_address',\n" +
                "       w.area   'warehouse_area'\n" +
                "FROM cargo c\n" +
                "         " +
                "JOIN warehouse w on c.warehouse_id = w.id\n" +
                "WHERE warehouse_id = ?", id);
    }

    public void saveCargo(CargoModelData model) {
        try (Connection connection = getConnection()) {
            String query;
            if (model.getId() == -1) {
                query = "INSERT INTO cargo (weight, area, warehouse_id, client_id) values (?,?,?,?)";
            } else {
                query = "UPDATE cargo set weight = ?,area = ?,warehouse_id = ?, client_id = ? where id = ?";
            }
            try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                statement.setInt(1, model.getWeight());
                statement.setInt(2, model.getArea());
                statement.setInt(3, model.getWarehouse().getId());
                statement.setInt(4, model.getClient().getId());
                if (model.getId() != -1)
                    statement.setInt(5, model.getId());
                int id = statement.executeUpdate();
                model.setId(id);
            }

        } catch (SQLException throwables) {
            Utils.showInternalError(throwables);
        }
    }

    public List<CargoModelData> getClientCargo(int id) {
        return getCargo("SELECT c.id,\n" +
                "       weight,\n" +
                "       " +
                "c.area,\n" +
                "       client_id,\n" +
                "       w.id     'warehouse_id',\n" +
                "       w.address 'warehouse_address',\n" +
                "       w.area   'warehouse_area'\n" +
                "FROM cargo c\n" +
                "         " +
                "JOIN warehouse w on c.warehouse_id = w.id\n" +
                "WHERE client_id = ?", id);
    }

    public Map<Integer, List<ContactDataModel>> getClientContact(int... ids) {
        Map<Integer, List<ContactDataModel>> map = new LinkedHashMap<>();
        try (Connection connection = getConnection()) {
            StringBuilder sql = new StringBuilder("SELECT client_id, ct.name, value\n" +
                    "from contact c\n" +
                    "join contact_type ct on c.contact_type_id = ct.id\n" +
                    "where client_id in (");
            for (int i = 0; i < ids.length; i++) {
                if (i > 0)
                    sql.append(",");
                sql.append("?");
            }
            try (PreparedStatement statement = connection.prepareStatement(sql + ")")) {
                for (int i = 0; i < ids.length; i++) {
                    statement.setInt(i + 1, ids[i]);
                }
                ResultSet result = statement.executeQuery();
                while (result.next()) {
                    int client = result.getInt("client_id");
                    List<ContactDataModel> list = map.computeIfAbsent(client, a -> new ArrayList<>(2));
                    list.add(new ContactDataModel(
                            client,
                            getContactTypeByName(result.getString("name")),
                            result.getString("value")));
                }
            }
        } catch (SQLException throwables) {
            Utils.showInternalError(throwables);
        }
        return map;
    }

    public void saveWarehouse(WarehouseDataModel model) {
        try (Connection connection = getConnection()) {
            @Language("SQL")
            String sql;
            if (model.getId() == -1)
                sql = "INSERT INTO warehouse (address, area) values (?,?)";
            else
                sql = "UPDATE warehouse set address = ?, area = ? where id = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, model.getAddress());
                statement.setInt(2, model.getArea());
                if (model.getId() != -1) {
                    statement.setInt(3, model.getId());
                }
                int id = statement.executeUpdate();
                if (model.getId() == -1) {
                    model.setId(id);
                }
            }
        } catch (SQLException throwables) {
            Utils.showInternalError(throwables);
        }
    }

    public List<ClientDataModel> getClients() {
        List<ClientDataModel> list = new ArrayList<>();
        try (Connection connection = getConnection()) {
            try (Statement statement = connection.createStatement()) {
                ResultSet result = statement.executeQuery("SELECT id, first_name, last_name, middle_name from client");
                while (result.next()) {
                    list.add(new ClientDataModel(
                            result.getInt("id"),
                            result.getString("first_name"),
                            result.getString("last_name"),
                            result.getString("middle_name")
                    ));
                }
            }
        } catch (SQLException throwables) {
            Utils.showInternalError(throwables);
            return null;
        }
        return list;
    }

    public void saveClient(ClientDataModel model) {
        try (Connection connection = getConnection()) {
            String query;
            if (model.getId() == -1)
                query = "INSERT INTO client (first_name, last_name, middle_name) values (?,?,?)";
            else
                query = "UPDATE client SET first_name = ?, last_name = ?, middle_name = ? where id = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, model.getFirstName());
                statement.setString(2, model.getLastName());
                statement.setString(3, model.getMiddleName());
                if (model.getId() != -1) {
                    statement.setInt(4, model.getId());
                }
                statement.executeUpdate();
                if (model.getId() == -1) {
                    try (Statement simpleStatement = connection.createStatement()) {
                        ResultSet generatedKeys = simpleStatement.executeQuery("SELECT last_insert_rowid()");
                        if (generatedKeys.next()) {
                            model.setId(generatedKeys.getInt(1));
                        }
                    }
                }
            }
        } catch (SQLException throwables) {
            Utils.showInternalError(throwables);
        }
    }

    public void saveClientContact(int id, List<ContactDataModel> contacts) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement delete = connection.prepareStatement("DELETE FROM contact where client_id = ?")) {
                delete.setInt(1, id);
                delete.executeUpdate();
            }
            try (PreparedStatement create = connection.prepareStatement(
                    "INSERT INTO contact (client_id, contact_type_id, value) VALUES (?,?,?)")) {
                if (contacts.size() > 0) {
                    for (ContactDataModel model : contacts) {
                        create.setInt(1, id);
                        create.setInt(2, model.getType().getValue().getId());
                        create.setString(3, model.getValue().getValue());
                        create.addBatch();
                    }
                    create.executeBatch();
                }
            }
        } catch (SQLException throwables) {
            Utils.showInternalError(throwables);
        }
    }

    public void deleteClient(int id) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM client where id = ?")) {
                statement.setInt(1, id);
                statement.executeUpdate();
            }
            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM cargo where client_id = ?")) {
                statement.setInt(1, id);
                statement.executeUpdate();
            }
            try (PreparedStatement statement = connection.prepareStatement("DELETE from contact where client_id = ?")) {
                statement.setInt(1, id);
                statement.executeUpdate();
            }
        } catch (SQLException throwables) {
            Utils.showInternalError(throwables);
        }
    }

    public void deleteWarehouse(int id) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM warehouse where id = ?")) {
                statement.setInt(1, id);
                statement.executeUpdate();
            }
            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM cargo WHERE id = ?")) {
                statement.setInt(1, id);
                statement.executeUpdate();
            }
        } catch (SQLException throwables) {
            Utils.showInternalError(throwables);
        }
    }
}


