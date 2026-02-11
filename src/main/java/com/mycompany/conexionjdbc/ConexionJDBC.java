/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.conexionjdbc;

import static com.mysql.cj.conf.PropertyKey.PASSWORD;
import static com.mysql.cj.conf.PropertyKey.USER;
import java.net.URL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


/**
 *
 * @author jealb
 */
public class ConexionJDBC {

    public static void main(String[] args) throws SQLException {
//        try(Connection con = DriverManager.getConnection(
//                "jdbc:mysql://localhost:3306/MyDataBase", "root", "16962Por")){
//            System.out.println("¡Conexion exitosa!");
//        }catch(SQLException e){
//            e.printStackTrace();
//        }
        crearTabla();
        
        System.out.println("\n---Inserciones---");
        insertar("Juan", "1234");
        insertar("Maria", "abcd");
        insertar("Pedro", "qwerty");
        insertar("Ana", "pass123");
        
        System.out.println("\n---Todos los clientes---");
        obtenerClientes();
        
        System.out.println("\n---Consulta por ID---");
        obtenerClientePorId(1);
        obtenerClientePorId(2);
        obtenerClientePorId(99);//no existe
        
        System.out.println("\n---Actualizacion--");
        actualizar(2, "Maria Actualizada", "newpass");
        obtenerClientePorId(2);
        
        System.out.println("\n---Eliminacion---");
        eliminar(3);
        obtenerClientes();
        
        System.out.println("\n---Login Nomal---");
        System.out.println("Login Juan/1234: " + login("Juan", "1234"));
        System.out.println("Login Ana/pass123: " + login("Ana", "pass123"));
        System.out.println("Login incorrecto: " + login("Ana", "wrong"));
        
        System.out.println("\n---Login seguro---\n");
        System.out.println("Login seguro Juan/1234: " + loginSeguro("Juan", "1234"));
        System.out.println("Login seguro Ana/pass123: " + loginSeguro("Ana", "pass123"));
        System.out.println("Login seguro incorrecto: " + loginSeguro("Ana", "wrong"));
        
        System.out.println("\n---Prueba SQL Injection---");
        System.out.println("Intento login normal: " + login("Juan", "' OR '1'='1"));//puede fallar
        System.out.println("Intento login seguro: " + loginSeguro("Juan", "' OR '1'='1"));
    }
    
  
    public static Connection getConnection() throws SQLException{
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/MyDataBase", "root", "16962Por");
    }
    
    public static void crearTabla(){
        String sql = "CREATE TABLE IF NOT EXISTS clientes("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "nombre VARCHAR(100),"
                + "password VARCHAR(100))";
        
        try (Connection con = getConnection(); Statement st = con.createStatement()) {
            st.execute(sql);
            System.out.println("Tabla creada con éxito.");
        }catch(SQLException e){
            e.printStackTrace();
        }
    }
    
    public static void obtenerClientes(){
            try(
                    Connection con = getConnection();
                    Statement stmt = con.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT id, nombre FROM clientes")
                ) {
                
                while(rs.next()){
                    int id = rs.getInt("id");
                    String nombre = rs.getString("nombre");
                    
                    System.out.println(id + " | " + nombre);
                }
            }catch(SQLException e){
                e.printStackTrace();
            }
    }
    
    public static boolean login(String nombre, String password){
        String sql = "SELECT * FROM clientes WHERE nombre='"
                + nombre + "' AND password='" + password + "'";
        
        try (
            Connection con = getConnection();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)
            ){
            return rs.next();
        } catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }
    
    public static boolean loginSeguro(String nombre, String password){
        String sql = "SELECT * FROM clientes WHERE nombre=? AND password=?";
        try (Connection con = getConnection();
                PreparedStatement ps= con.prepareStatement(sql)
            ){
            ps.setString(1,nombre);
            ps.setString(2, password);
            try(ResultSet rs = ps.executeQuery()){
                return rs.next();
            }
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }
    
    public static void insertar(String nombre, String password){
        String sql = "INSERT INTO clientes (nombre, password) VALUES (?, ?)";
        try (
                Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
            ){
            
            ps.setString(1, nombre);
            ps.setString(2, password);
            
            int filas = ps.executeUpdate();
            
            if(filas > 0){
                try(ResultSet rs = ps.getGeneratedKeys()){
                    while (rs.next()){
                        System.out.println("Insertado cliente con ID: " + rs.getInt(1));
                    }
                }
            }
        } catch(SQLException e){
            e.printStackTrace();
        }
    }
    
    public static void obtenerClientePorId(int id){
        String sql = "SELECT id, nombre, password FROM clientes WHERE id = ?";
        try(Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
            ){
            
            ps.setInt(1, id);
            
            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    System.out.println("ID: " + rs.getInt("id")
                        + ", Nombre: " + rs.getString("nombre")
                        + ", Password: " + rs.getString("password"));
                }else{
                    System.out.println("No se encontró cliente con ID: " + id);
                }
            } 
        }catch(SQLException e){
            e.printStackTrace();
        }
    }
    
    public static void actualizar(int id, String nuevoNombre, String nuevoPassword){
        String sql = "UPDATE clientes SET nombre = ?, password = ? WHERE id = ?";
        try(Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)){
            
            ps.setString(1, nuevoNombre);
            ps.setString(2, nuevoPassword);
            ps.setInt(3, id);
            
            int filas = ps.executeUpdate();
            
            if (filas > 0){
                System.out.println("Cliente actualizado con ID: " + id);
            } else {
                System.out.println("No se encontró cliente con ID: " + id);
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
    
    public static void eliminar(int id){
        String sql = "DELETE FROM clientes WHERE id = ?";
        try (Connection con = getConnection(); PreparedStatement ps =con.prepareStatement(sql)){
            
            ps.setInt(1, id);
            
            int filas = ps.executeUpdate();
            
            if (filas > 0){
                System.out.println("Cliente eliminado con ID: " + id);
            } else {
                System.out.println("No se encontró cliente con ID: " + id);
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
}
