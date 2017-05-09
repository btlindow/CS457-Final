/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mlsdbms;
import java.util.Random;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
/**
 *
 * @author Home
 */
public class Table {
    private int rows;
	private int cols;
	private String colName;
	private short [][] table;
	private String [] metadata;
        
        public Table(String inputFile) {
            init(inputFile);
        }
	
	public Table(int r,int c) {
            setRows(r);
            setCols(c);
            table = new short[r][c];
            metadata = new String[c];
	}
	
	public String getcolName() {
            return colName;
	}
	
	public void setcolName(String colName) {
            this.colName = colName;
	}

	public int getRows() {
            return rows;
	}

	public void setRows(int rows) {
            this.rows = rows;
	}

	public int getCols() {
            return cols;
	}

	public void setCols(int cols) {
            this.cols = cols;
	}
	
	public String[] getWholeMeta() {
            return metadata;
	}
	
	public void setWholeMeta(String[] meta) {
            metadata = new String[meta.length];
            System.arraycopy(meta, 0, metadata, 0, meta.length);
	}
        
        private void init(String file) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                int numRows = 0;
                int numCols;
                
                
                
                line = br.readLine();
                
                String[] str = line.split("\t");
                colName = str[0].substring(0, 1);
                for(int i = 0; i < str.length; i++) {
                    if(str[i].equals("TC")) {
                        str[i] = colName + str[i];
                    }
                    if(str[i].equals("KC")) {
                        str[i] = colName + str[i];
                    }
                }
                
                metadata = new String[str.length];
                System.arraycopy(str, 0, metadata, 0, str.length);
                numCols = str.length;
               
                while((line = br.readLine()) != null) {
                    numRows++;
                }
                
                table = new short[numRows][numCols];
                setRows(numRows);
                setCols(numCols);
                
                int rowIndex = 0;
                
                br = new BufferedReader(new FileReader(file));
                br.readLine();
                
                while((line = br.readLine()) != null) {
                    String[] parts = line.split("\t");
                    for (int i = 0; i < numCols; i++) {
                        table[rowIndex][i] = Short.parseShort(parts[i]);
                    }
                    rowIndex++;
                }
            } catch (IOException e) {
            }
        }
	
	public void input(int r, int c, short data) {
            table[r][c] = data;
	}
	
	public short getData(int r, int c) {
            return table[r][c];
	}
	
	public void setMeta(int i, String str) {
            metadata[i] = str;
	}
	
	public String getMeta(int i) {
            return metadata[i];
	}
	
	public void show() {
            if(getRows() == 0) {
                System.out.println("No Results");
                return;
            }
            printBar();
            for (int i = 0; i < cols; i++) {
                    if((metadata[i].substring(1)).equals("KC")) {
                        System.out.print("KC" + "\t");
                    } else {
                        System.out.print(metadata[i] + "\t");
                    }
                    if (i == cols - 1) {
                        System.out.print(" |");
                    }
            }
            System.out.println();
            printBar();
            for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                            System.out.print(table[i][j] + "\t");
                            if (j == cols-1) {
                                System.out.print(" |");
                            }
                    }
                    System.out.println();
            }
            printBar();
            System.out.println("Results: " + getRows());
            System.out.println();
	}
        
        private void printBar() {
            for(int i = 0; i < getCols() - 1; i++) {
               System.out.print("-");
            }
            for (int i = 0; i < getCols(); i++) {
                System.out.print("-------");
            }
            for (int i = 0; i < getMeta(getCols() - 1).length(); i++) {
                System.out.print("-");
            }
            System.out.println();
        }
        
}
