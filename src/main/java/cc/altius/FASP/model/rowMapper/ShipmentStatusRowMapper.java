/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cc.altius.FASP.model.rowMapper;

import cc.altius.FASP.model.Label;
import cc.altius.FASP.model.NextShipmentStatusAllowed;
import cc.altius.FASP.model.ShipmentStatus;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

/**
 *
 * @author palash
 */
public class ShipmentStatusRowMapper implements ResultSetExtractor<List<ShipmentStatus>> {

    @Override
    public List<ShipmentStatus> extractData(ResultSet rs) throws SQLException, DataAccessException {
        List<ShipmentStatus> shipmentStatusList = new LinkedList<>();
        NextShipmentStatusAllowed nextShipmentStatusAllowed;
        ShipmentStatus ss;
        while (rs.next()) {
            ss = new ShipmentStatus();
            ss.setShipmentStatusId(rs.getInt("SHIPMENT_STATUS_ID"));
            if (shipmentStatusList.indexOf(ss) == -1) {
                ss.setActive(rs.getBoolean("ACTIVE"));
                ss.setLabel(new Label(rs.getInt("LABEL_ID"), rs.getString("LABEL_EN"), rs.getString("LABEL_SP"), rs.getString("LABEL_FR"), rs.getString("LABEL_PR")));
                shipmentStatusList.add(ss);
            }
            ss = shipmentStatusList.get(shipmentStatusList.indexOf(ss));
            nextShipmentStatusAllowed = new NextShipmentStatusAllowed();
            if (rs.getInt("NEXT_SHIPMENT_STATUS_ID") != 0) {
                nextShipmentStatusAllowed.setShipmentStatusAllowedId(rs.getInt("NEXT_SHIPMENT_STATUS_ID"));
                if (ss.getNextShipmentStatusAllowedList().indexOf(nextShipmentStatusAllowed) == -1) {
//                nextShipmentStatusAllowed.setNextShipmentStatusId(rs.getInt("NEXT_SHIPMENT_STATUS_ID"));
                    ss.getNextShipmentStatusAllowedList().add(nextShipmentStatusAllowed);

                }
            }

        }

        return shipmentStatusList;
    }
}

//    @Override
//    public ShipmentStatus mapRow(ResultSet rs, int i) throws SQLException {
//        ShipmentStatus ss= new ShipmentStatus();
//        ss.setActive(rs.getBoolean("ACTIVE"));
//        ss.setShipmentStatusId(rs.getInt("SHIPMENT_STATUS_ID"));
//        Label l= new Label();
//        l.setLabelId(rs.getInt("LABEL_ID"));
//        l.setEngLabel(rs.getString("LABEL_EN"));
//        l.setFreLabel(rs.getString("LABEL_FR"));
//        l.setSpaLabel(rs.getString("LABEL_SP"));
//        l.setPorLabel(rs.getString("LABEL_PR"));
//        ss.setLabel(l);
//        return ss;
//    }
//    }
