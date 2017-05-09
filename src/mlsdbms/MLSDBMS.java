/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mlsdbms;

/**
 *
 * @author Home
 */
public class MLSDBMS {

    public static Database db;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        db = new Database(args[0]);
        while(true) {
            db.prompt();
        }
    }
    
}
