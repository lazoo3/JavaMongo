import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.bson.types.ObjectId;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;


/**
 * Created by Group 1 on 26.3.2017..
 */
public class SearchGUI extends JFrame{
    private HashMap<String[], List<String>> map = new HashMap<>();
    List<Document> getComments;
    JTextField searchText;
    JButton searchButton;
    JComboBox comboBox;
    JPanel searchPanel;
    JTable searchTable;
    private JScrollPane scrollPane;
    private JTable showTable;
    private JScrollPane showPane;
    private JPanel jpShow;
    private JButton updateButton;
    private JTextArea commentBox;
    private JPanel jpUpdate;
    private  Document getDoc;
    String value;
    MongoDatabase myDB = null;
    MongoClient client = null;
    MongoCollection<Document> collection = null;
    MongoCursor<Document> cursor = null;
    SearchGUI gui = null;
    String[] headerline = {"_id","Director Name","Movie Title","Genre"};
    String[] headerline2 = {"_id","Director Name","Movie Title","Duration","Year","Gross","Genres","Total Facebook Likes","Language", "Comment"};
    DefaultTableModel model;
    DefaultTableModel model2;
    String comboText = null;
    String getImage;
    int row;
    ObjectId getId;
    public SearchGUI() {
        lookAndFeel();
        showPane.setVisible(false);
        model = new DefaultTableModel(headerline, 0);
        searchTable.setEnabled(false);

        searchTable.setModel(model);
        searchTable.setAutoCreateRowSorter(true);
        searchTable.getColumnModel().getColumn(0).setMinWidth(0);
        searchTable.getColumnModel().getColumn(0).setMaxWidth(0);

        model2 = new DefaultTableModel(headerline2, 0);
        showTable.setEnabled(false);
        showTable.setModel(model2);
        showTable.setAutoCreateRowSorter(true);

        jpUpdate.setVisible(false);
        comboBox.addItem("Director Name");
        comboBox.addItem("Movie Title");
        comboBox.addItem("Genre");
        setSize(1300,700);
        setTitle("Search IMDB");
        setContentPane(searchPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        connectMongo();
        searchButton.addActionListener(new SearchDB());

        searchTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                searchTable = (JTable) e.getComponent();
                int col = searchTable.columnAtPoint(e.getPoint());
                row = searchTable.rowAtPoint(e.getPoint());
                searchTable.setRowSelectionInterval(row, row);

                if(e.getClickCount() == 2){
                    if (col < 0 || row < 0) {

                        System.out.println("Wrong col or row pressed");
                    } else {
                        if(model2.getRowCount() > 0){
                            for (int i = model2.getRowCount() -1; i > -1;i--){
                                model2.removeRow(i);
                            }
                        }
                        showPane.setVisible(true);
                        validate();
                        int row = searchTable.getSelectedRow();
                        value = searchTable.getModel().getValueAt(row, 0).toString();


                        showData(value);

                    }
                }
            }
        });

        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                addComment(commentBox.getText());
                if(model2.getRowCount() > 0){
                    for (int i = model2.getRowCount() -1; i > -1;i--){
                        model2.removeRow(i);
                    }
                }
                validate();
                showData(getId.toString());
            }
        });
        searchText.addActionListener(new SearchDB());

        showTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showComment();
            }
        });
    }



    class SearchDB implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            model.setRowCount(0);
            searchTable.setModel(model);
            searchBar(searchText.getText(), comboBox.getSelectedItem().toString());
        }
    }


    public static void main(String[] args){
        SearchGUI mongo = new SearchGUI();
    }
    public void connectMongo(){
        client = new MongoClient("localhost",27017);

        myDB = client.getDatabase("projectdb");

        collection = myDB.getCollection("projectcoll");
        System.out.println("connected");
    }

    public void lookAndFeel(){
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (UnsupportedLookAndFeelException e) {
            // handle exception
        } catch (ClassNotFoundException e) {
            // handle exception
        } catch (InstantiationException e) {
            // handle exception
        } catch (IllegalAccessException e) {
            // handle exception
        }
    }
    public void searchBar(String getText, String getBox){

        switch (getBox){
            case "Movie Title":
                comboText = "movie_title";
                break;
            case "Director Name":
                comboText = "director_name";
                break;
            case "Genre":
                comboText = "genres";
                break;
            default:
                System.out.println("Error");
        }
        BasicDBObject query = new BasicDBObject();
        query.append(comboText,new BasicDBObject("$regex", Pattern.compile("\\b"+getText,Pattern.CASE_INSENSITIVE)));
        cursor = collection.find(query).iterator();

        while (cursor.hasNext()){
            Document obj = cursor.next();

            String getId = String.valueOf(obj.get("_id"));
            String directorName = obj.getString("director_name");
            String movieTitle = String.valueOf(obj.get("movie_title"));
            String genres = String.valueOf(obj.get("genres"));

            model.addRow(new Object[]{getId, directorName,movieTitle, genres});
        }
        searchTable.setModel(model);
        jpUpdate.setVisible(false);
        showPane.setVisible(false);
        validate();
    }
    public void showData(String value){

        System.out.println(new ObjectId(value));
        BasicDBObject query = new BasicDBObject();
        query.append("_id", new ObjectId(value));
        cursor = collection.find(query).iterator();

        while (cursor.hasNext()){
            Document obj = cursor.next();
            getDoc = obj;
            getComments =  (List) obj.get("comments");
            String getId = String.valueOf(obj.get("_id"));
            String directorName = obj.getString("director_name");
            String movieTitle = String.valueOf(obj.get("movie_title"));
            String duration = String.valueOf(obj.get("duration"));
            String gross = String.valueOf(obj.get("gross"));
            String genres = obj.getString("genres");
            String facebookLikes = String.valueOf(obj.get("movie_facebook_likes"));
            String language = obj.getString("language");
            String titleYear = String.valueOf(obj.get("title_year"));
            getImage = String.valueOf(obj.get("image"));
            String comment = "";
            if(getComments != null) {
                for (Object o : getComments) {
                    Document doc = (Document) o;
                    comment += (String) doc.get("comment") + ", ";

                }
            }

            model2.addRow(new Object[]{getId,directorName,movieTitle, duration+" Minutes",titleYear, gross+" $", genres, facebookLikes, language, comment});

        }

        //showData();


        jpUpdate.setVisible(true);
        showTable.getColumnModel().getColumn(0).setMinWidth(0);
        showTable.getColumnModel().getColumn(0).setMaxWidth(0);
        showTable.setModel(model2);
        validate();
    }
    public void addComment(String commentText){

        getId = getDoc.getObjectId("_id");
        Document addComment = new Document();
        List<Document> commentList = new ArrayList<Document>();
        if(getDoc.get("comments") != null){
            commentList = (List<Document>)getDoc.get("comments");
        }
        addComment.append("comment", commentText);
        commentList.add(addComment);
        collection.updateOne(new Document("_id",getId),new Document("$set",new Document("comments",commentList)), new UpdateOptions().upsert(true));


    }

    public void showComment(){
        String comment = "";
        String getSplit = "";
        if(getComments != null) {
            for (Object o : getComments) {
                Document doc = (Document) o;
                comment += "Comment: " + (String) doc.get("comment") + ",";

            }
        }
        getSplit += "<html>";
        for (String split : comment.split(",")) {
                getSplit += split + "<br>";
        }
        System.out.println(getImage);
        InputStream is = SearchGUI.class.getClassLoader().getResourceAsStream(getImage);
        Icon icon = null;
        try {
            icon = new ImageIcon(ImageIO.read(is));
        } catch (IOException e) {
            e.printStackTrace();
        }
        getSplit += "</html>";
        JOptionPane.showMessageDialog(this, new JLabel(getSplit, icon, JLabel.LEFT),"Comments and Image", JOptionPane.INFORMATION_MESSAGE);
    }
}
