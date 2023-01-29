package com.company;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

import java.sql.SQLException;
import java.util.HashSet;

public class Crawler {
    public HashSet<String> urlHash;
    public int MAX_DEPTH=2;
    public static Connection connection= null;

    public Crawler(){
        //initialize urlHash
        urlHash=new HashSet<>();
        connection=DatabaseConnection.getConnection();
    }
//to save web pages data into database
    public void getPageTextAndLink(String url,int depth){
        if(!urlHash.contains(url)){
            if(urlHash.add(url)) {
                System.out.println(url);
            }
            try {
                Document document = Jsoup.connect(url).timeout(5000).get();

                String text= document.text().length()>1000?document.text().substring(0,999):document.text();
                //get title of web page
                String title=document.title();

                //we are just prepared insertion commend

                PreparedStatement preparedStatement=connection.prepareStatement("Insert into pages values(?,?,?)");
                preparedStatement.setString(1,title);
                preparedStatement.setString(2,url);
                preparedStatement.setString(3,text);

                preparedStatement.executeUpdate();
                //System.out.println(title+"\n"+text);
                //increasing depth
                depth++;

                if(depth==2){
                    return;
                }
                Elements availableLinkesOnPage=document.select("a[href]");
                for (Element currentLink : availableLinkesOnPage) {
                    getPageTextAndLink(currentLink.attr("abs:href"),depth);


                }

            }
            catch(IOException | SQLException ioException){
                ioException.printStackTrace();
            }
        }
    }
    public static void main(String[] args) {
        Crawler crawler=new Crawler();

        crawler.getPageTextAndLink("https://www.javatpoint.com",0);

    }
}
