package com.example.application.views;

import com.example.application.data.entity.Film;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


@PageTitle("Upload ELA-Favoriten | by DBUSS GmbH")
@Route(value = "ela-upload", layout= MainLayout.class)
public class ElaFavoritenView extends VerticalLayout {

    public ElaFavoritenView(){
        Grid<Film> grid = new Grid<>(Film.class);
        add(new H1("Upload der ELA-Favoriten Excel-Liste"));

     //   EntityManagerFactory factory = Persistence.createEntityManagerFactory("JPAKursprojekt");
     //   EntityManager em = factory.createEntityManager();

        List<Film> customerList = new ArrayList<>();
        try{
            //step1 load the driver class
            Class.forName("oracle.jdbc.driver.OracleDriver");

            //step2 create  the connection object
            Connection con= DriverManager.getConnection("jdbc:oracle:thin:@37.120.189.200:1521:xe","SYSTEM","Michael123");

            //step3 create the statement object
            Statement stmt=con.createStatement();

            //step4 execute query
            ResultSet rs=stmt.executeQuery("select Film_ID, Film_Name from FILM");
            while(rs.next()) {
                Film customer = new Film();
                customer.setFilm_ID(Integer.getInteger(rs.getString(1)));
                customer.setFilm_Name(rs.getString(2));

                customerList.add(customer);
            }


            //step5 close the connection object
            con.close();

        }catch(Exception e){ System.out.println(e);}



        //GRID
      //  grid.setColumns("Film_ID", "Film_Name");
        grid.setItems(customerList);
        add(grid);


    }
}
