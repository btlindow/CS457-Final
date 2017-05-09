/*
 *  Ben Lindow
 *  CS457 - Final Project
 */
package mlsdbms;
import java.util.Scanner;
import java.util.regex.Pattern;
/**
 *
 * @author Home
 */
public class Database {
    public static Scanner sc;
	public static int level;
        public static int dflag;
        public static String[] KCs;
	public static String select, from, where;
	public static Table table[] = new Table[3];
	
	public Database(String folder) {
		table[0] = new Table(folder + "/T1.txt");
		table[1] = new Table(folder + "/T2.txt");
		table[2] = new Table(folder + "/T3.txt");
	}
	
	public void show() {
		table[0].show();
		table[1].show();
		table[2].show();
	}
	
	public void prompt() {
		sc = new Scanner(System.in);
                level = Integer.parseInt(sc.nextLine());
                sc.useDelimiter(Pattern.compile(";"));
                String query = sc.next();
                query = query.toUpperCase();
                
                String[] parts = query.split("\n| |, |\t");
                
                select = "";
                from = "";
                where = "";
                
                int i;
                for(i = 1; i < parts.length; i++) {
                    if(!parts[i].equals("FROM") && !parts[i].equals("from")) {
                        if (select.equals("")) {
                            select = parts[i];
                        }else {
                            select += (", " + parts[i]);
                        }
                    }else{
                        break;
                    }
                }
                
                for(i = i + 1; i < parts.length; i++) {
                    if(!parts[i].equals("WHERE") && !parts[i].equals("where")) {
                        if (from.equals("")) {
                            from = parts[i];
                        }else {
                            from += (", " + parts[i]);
                        }
                    }else{
                        break;
                    }
                }
                
                for(i = i + 1; i < parts.length; i++) {
                    if (where.equals("")) {
                            where = parts[i];
                        }else {
                            where += (" " + parts[i]);
                        }
                }
                    
		process();
	}
        
        private static String[] whereKC(String[] whers, int tab) {
            for(int i = 0; i < whers.length; i++) {
                if (whers[i].equals("KC")) {
                    whers[i] = table[tab].getcolName() + "KC";
                }
            }
            return whers;
        }
        
        private static String[] includeKC(String[] sels) {
            int count = 0;
            for(int i = 0; i < sels.length; i++) {
                if ((sels[i].substring(1)).equals("1")) {
                    count++;
                }
            }
            if(count > 0) {
                KCs = new String[count];
            }
            else {
                return sels;
            }
            count = 0;
            for (int i = 0; i < sels.length; i++) {
                if ((sels[i].substring(1)).equals("1")) {
                    String let = String.valueOf(sels[i].charAt(0));
                    KCs[count++] = let + "KC";
                }
            }
            String[] fin = new String[count + sels.length];
            int finIndex = 0;
            int kcIndex = 0;
            for(int i = 0; i < sels.length; i++) {
                if ((sels[i].substring(1)).equals("1")) {
                    fin[finIndex] = sels[i];
                    finIndex++;
                    fin[finIndex++] = KCs[kcIndex];
                    kcIndex++;
                }
                else {
                    fin[finIndex++] = sels[i];
                }
            }
            return fin;
        }
        
	private static void process() {
		String[] colNames   = select.split(", ");
		String[] tableNames = from.split(", ");
		String[] whereConds = where.split("\\=| and | AND ");
                int a = Integer.parseInt(tableNames[0].substring(1)) - 1;
                whereConds = whereKC(whereConds, a);
                
                offlimits(whereConds);
                colNames = includeKC(colNames);
                
                Scanner sc = new Scanner(System.in);
               
		Table t = combineTables(tableNames);
                debug(t, "Finished Cartesian Product");
                
		if (t==null) {
                    return;
		}
                
                t = applyKC(t, tableNames.length);
                debug(t, "Finished Applying KC Rules");
                
                t = applyTC(t, tableNames.length);
                debug(t, "Finished Applying TC Rules");
                
                if(!whereConds[0].equals("")) {
                    int numClauses = (whereConds.length/2);
                    int left = 0;
                    int right = 1;
                    for (int i = 0; i < numClauses; i++) {
                       	t = trimRows(t, new String[] {whereConds[left], whereConds[right]});
			left+=2;
			right+=2;
                   } 
                }
                debug(t, "Finished Projection Operation");
                
                t = trimColumns(t, colNames);
                debug(t, "Finished Selection Operation");
               
                t = hideLevel(t, level);
                debug(t, "Finished Removing Higher TC");
                
		t.show();
	}
        
        private static void offlimits(String [] whereConds) {
            if(!whereConds[0].equals("")) {
                    int numClauses = (whereConds.length/2);
                    int left = 0;
                    int right = 1;
                    for (int i = 0; i < numClauses; i++) {
                       	if (whereConds[left].equals("TC") || whereConds[left].equals("KC")) {
                            if (Integer.parseInt(whereConds[right]) > level) {
                                System.out.println("Outside Security Level Query");
                                System.exit(1);
                            }
                        }
			left+=2;
			right+=2;
                   } 
                }
        }
        
        private static void debug(Table t, String message) {
            if(dflag == 1) {System.out.println(message);}
            if(dflag == 2) {
                Scanner sc = new Scanner(System.in);
                t.show();
                System.out.println("Finished " + message);
                System.out.println("Press Enter to Step: ");
                sc.nextLine();
            }
        }
        
        private static Table hideLevel(Table p, int lev) {
            int count = 0;
            for (int i = 0; i < p.getRows(); i++) {
                if (lev >= p.getData(i, p.getCols()-1)) {
                    count++;
                }
            }
            Table t = new Table(count, p.getCols());
            t.setWholeMeta(p.getWholeMeta());
            int index = 0;
            for (int i = 0; i < p.getRows(); i++) {
                if (lev >= p.getData(i, p.getCols()-1)) {
                    for (int j = 0; j < t.getCols(); j++) {
                        t.input(index, j, p.getData(i, j));
                    }
                    index++;
                }
            }
            return t;
        }
        
        private static Table applyKC(Table p, int numTabs) {
            if (numTabs == 1) {
                int colNum = 0;
                for(int i = 0; i < p.getCols(); i++) {
                    if((p.getMeta(i)).substring(1).equals("KC")) {
                        colNum = i;
                    }
                }
                int count = 0;
                for (int i = 0; i < p.getRows(); i++) {
                    int c = p.getData(i, colNum);
                    if (c <= level){
                        count++;
                    }
                }
                Table t = new Table(count, p.getCols());
                t.setWholeMeta(p.getWholeMeta());
                int index = 0;
                for(int i = 0; i < p.getRows(); i++) {
                    int c = p.getData(i, colNum);
                    if (c <= level) {
                        for (int j = 0; j < p.getCols(); j++) {
                            t.input(index, j, p.getData(i, j));
                        }
                        index++;
                    }
                }
                return t;
            }
            int index = 0;
            int[] secCol = new int[numTabs];
            for(int i = 0; i < p.getCols(); i++) {
                if((p.getMeta(i).substring(1)).equals("KC")) {
                    secCol[index++] = i;
                }
            }
            int count = 0;
            for (int i = 0; i < p.getRows(); i++) {
                if (numTabs == 2) {
                    int l = p.getData(i, secCol[0]);
                    int r = p.getData(i, secCol[1]);
                    if (l == r && l <= level) {
                        count++;
                    }
                }
                if (numTabs == 3) {
                    int l = p.getData(i, secCol[0]);
                    int m = p.getData(i, secCol[1]);
                    int r = p.getData(i, secCol[2]);
                    if (l == m && m == r && l <= level) {
                        count++;
                    }
                }
            }
            index = 0;
            Table t = new Table(count, p.getCols());
            t.setWholeMeta(p.getWholeMeta());
            for(int i = 0; i < p.getRows(); i++) {
                if (numTabs == 2) {
                    int l = p.getData(i, secCol[0]);
                    int r = p.getData(i, secCol[1]);
                    if (l == r && l <= level) {
                        for (int j = 0; j < p.getCols(); j++) {
                            t.input(index, j, p.getData(i, j));
                        }
                        index++;
                    }
                }
                if (numTabs == 3) {
                    int l = p.getData(i, secCol[0]);
                    int m = p.getData(i, secCol[1]);
                    int r = p.getData(i, secCol[2]);
                    if (l == m && m == r && l <= level) {
                        for (int j = 0; j < p.getCols(); j++) {
                            t.input(index, j, p.getData(i, j));
                        }
                        index++;
                    }
                }
            }
            return t;
        }
        
        private static Table applyTC(Table p, int numTabs) {
            if (numTabs == 1) {
                for (int i = 0; i < p.getCols(); i++) {
                    if((p.getMeta(i).substring(1)).equals("TC")) {
                        p.setMeta(i, "TC");
                        return p;
                    }
                }
            }
            int index = 0;
            int[] secCol = new int[numTabs];
            for(int i = 0; i < p.getCols(); i++) {
                if((p.getMeta(i).substring(1)).equals("TC")) {
                    secCol[index++] = i;
                }
            }
            
            index = 0;
            Table t = new Table(p.getRows(), (p.getCols() - (numTabs - 1)));
            index = 0;
            for (int i = 0; i < p.getRows(); i++) {
                for (int j = 0; j < p.getCols(); j++) {
                    if(!(p.getMeta(j).substring(1)).equals("TC")) {
                        t.input(i, index++, p.getData(i, j));
                    }
                }
                index = 0;
            }
            for(int i = 0; i < t.getRows(); i++) {
                if (numTabs == 2) {
                    t.input(i, t.getCols() - 1, (short) Math.max(p.getData(i, secCol[0]), p.getData(i, secCol[1])));
                }
                else {
                    t.input(i, t.getCols() - 1, (short) Math.max(Math.max(p.getData(i, secCol[0]), p.getData(i, secCol[1])), p.getData(i, secCol[2])));
                }
            }
            index = 0;
            for (int i = 0; i < p.getCols() - 1; i++) {
                if(!(p.getMeta(i).substring(1)).equals("TC")) {
                    t.setMeta(index++, p.getMeta(i));
                }
            }
            t.setMeta(t.getCols() - 1, "TC");
            return t;
            
        }
	
	private static Table trimRows(Table p, String[] whereConds) {
                try {
                    int val = Integer.parseInt(whereConds[1]);
                    int colNum = 0;
                    for (int i = 0; i < p.getCols(); i++) {
                        if (whereConds[0].equals(p.getMeta(i))) {
                            colNum = i;
                            break;
                        }
                    }
                    int count = 0;
                    for(int r = 0; r < p.getRows(); r++) {
                        if(p.getData(r, colNum) == val) {
                            count++;
                        }
                    }
                    Table t = new Table(count, p.getCols());
                    t.setWholeMeta(p.getWholeMeta());
                    int rowIndex = 0;
                    for (int r = 0; r < p.getRows(); r++) {
                        if(p.getData(r, colNum) == val) {
                            for (int c = 0; c < t.getCols(); c++) {
                                t.input(rowIndex, c, p.getData(r, c));
                            }
                            rowIndex++;
                        }
                    }
                    return t;
                } catch(NumberFormatException e) {
                    int[] colNum = new int[2];
		int count = 0;
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < p.getCols(); j++) {
				if(whereConds[i].equals(p.getMeta(j))) {
					colNum[i] = j;
					break;
				}
			}
		}
		for (int i = 0; i < p.getRows(); i++) {
			if (p.getData(i, colNum[0]) == p.getData(i, colNum[1])) {
				count++;
			}
		}
		Table t = new Table(count, p.getCols());
		t.setWholeMeta(p.getWholeMeta());
		int tIndex = 0;
		for(int i = 0; i < p.getRows(); i++) {
			if (p.getData(i, colNum[0]) == p.getData(i, colNum[1])) {
				for (int j = 0; j < t.getCols(); j++) {
					t.input(tIndex, j, p.getData(i, j));
				}
				tIndex++;
			}
		}
		return t;
            }
        }
	
	private static Table trimColumns(Table p, String[] colNames) {
            if (colNames[0].equals("*")) {
                    return p;
            }
            
            boolean TCExists = false;
            for (int i = 0; i < colNames.length; i++) {
                if(colNames[i].equals("TC")) {
                    TCExists = true;
                }
            }
            
            if(!TCExists) {
                String[] str = new String[colNames.length + 1];
                System.arraycopy(colNames, 0, str, 0, colNames.length);
                str[colNames.length] = "TC";
                colNames = str;
            }
            
            
            int colNum = colNames.length;
            Table t = new Table(p.getRows(), colNum);
            for(int i = 0; i < colNum; i++) {
                t.setMeta(i, colNames[i]);
            }
            int[] cols = new int [colNum];
            for(int i = 0; i < colNum; i++) {
                for (int j = 0; j < p.getCols(); j++) {
                        if (colNames[i].equals(p.getMeta(j))) {
                            cols[i] = j;
                            break;
                    }
                }
            }
            int pIndex = 0;
            for(int r = 0; r < t.getRows(); r++) {
                for(int c = 0; c < colNum; c++) {
                    t.input(r, c, p.getData(r, cols[pIndex++]));
                }
                pIndex = 0;
            }
            return t;
	}
	
	private static Table combineTables(String[] tableNames) {
            int tCount = tableNames.length;
            switch (tCount) {
                case 1:
                    if(tableNames[0].equals("")) {
                        System.out.println("No Table Specified");
                        return null;
                    }
                    int a = Integer.parseInt(tableNames[0].substring(1)) - 1;
                    return table[a];
                case 2:
                    int b = Integer.parseInt(tableNames[0].substring(1)) - 1; 
                    int c = Integer.parseInt(tableNames[1].substring(1)) - 1;
                    return cartesianProduct(table[b], table[c]);
                case 3:
                    return cartesianProduct(cartesianProduct(table[0], table[1]), table[2]);
                default:
                    System.out.println("Too many tables specified");
                    return null;
            }
	}
	
	private static Table cartesianProduct(Table tableA, Table tableB) {
		Table product;
		int pRows = tableA.getRows() * tableB.getRows();
		int pCols = tableA.getCols() + tableB.getCols();
		product = new Table(pRows, pCols);
		int row = 0;
		int bcol = 0;
		for (int i = 0; i < tableA.getRows(); i++) {
                    for (int j = 0; j < tableB.getRows(); j++) {
                            for (int c = 0; c < tableA.getCols(); c++) {
                                    product.input(row, c, tableA.getData(i, c));
                            }
                            row++;
                    }
		}
		row = 0;
		for (int i = 0; i < tableA.getRows(); i++) {
                    for (int j = 0; j < tableB.getRows(); j++) {
                            for (int c = tableA.getCols(); c < pCols; c++) {
                                    product.input(row, c, tableB.getData(j, bcol++));
                            }
                            row++;
                            bcol = 0;
                    }
		}
		for(int i = 0; i < tableA.getCols(); i++)
		{
                    product.setMeta(i, tableA.getMeta(i));
		}
		bcol = 0;
		for (int i = tableA.getCols(); i < pCols; i++) {
                    product.setMeta(i, tableB.getMeta(bcol++));
		}		
		return product;
	}
}
