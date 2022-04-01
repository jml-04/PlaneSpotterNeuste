package planespotter.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import planespotter.constants.SqlQuerrys;
import planespotter.dataclasses.Frame;

public class DBIn {

	private static Connection getDBConnection() throws Exception {
		Class.forName("com.mysql.cj.jdbc.Driver");
		String db = "jdbc:sqlite:plane.db";
		Connection conn = DriverManager.getConnection(db);
		return conn;
	}

	public static void insertPlane(Frame f) throws Exception {
		Connection conn = getDBConnection();
		String planeFilter = "SELECT icaonr from planes WHERE icaonr = " + f.getIcaoAdr() + " LIMIT 1";
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(planeFilter);
		String icao = new String();
		while (rs.next()) {icao = rs.getString(0);}

		if (icao == null) {
			// insert into planes
			PreparedStatement pstmt = conn.prepareStatement(SqlQuerrys.planequerry);
			pstmt.setString(1, f.getIcaoAdr());
			pstmt.setString(2, f.getTailnr());
			pstmt.setString(3, f.getRegistration());
			pstmt.setString(4, f.getPlanetype());
			//TODO Airline ID anfrage
			pstmt.setString(5, f.getAirline());
			pstmt.executeUpdate();

		} else {

		}
	}

	public static void insertFlight(Frame f) throws Exception {
		Connection conn = getDBConnection();
		PreparedStatement pstmt = conn.prepareStatement(SqlQuerrys.flightquerry);
		pstmt.setString(1, f.getIcaoAdr());
		pstmt.setString(2, f.getSrcAirport());
		pstmt.setString(3, f.getDestAirport());
		pstmt.setString(4, f.getFlightnumber());
		pstmt.setString(5, f.getCallsign());
		pstmt.executeUpdate();
	}

	public static void insertTracking(Frame f) throws Exception {
		// get FlightID for
		Connection conn = getDBConnection();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(SqlQuerrys.getLastFlightID);
		int flightid = 0;
		while (rs.next()) {flightid = rs.getInt("ID");}
		// insert into tracking
		PreparedStatement pstmt = conn.prepareStatement(SqlQuerrys.trackingquerry);
		pstmt.setInt(1, flightid);
		pstmt.setDouble(2, f.getLat());
		pstmt.setDouble(3, f.getLon());
		pstmt.setInt(4, f.getAltitude());
		pstmt.setInt(5, f.getGroundspeed());
		pstmt.setInt(6, f.getHeading());
		pstmt.setInt(7, f.getSquawk());
		//TODO ADD TIMESTAMP
		pstmt.executeUpdate();
	}

}
