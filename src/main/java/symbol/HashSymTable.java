/**
 * This code is part of the lab exercises for the Compilers course at Harokopio
 * University of Athens, Dept. of Informatics and Telematics.
 */
package symbol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HashSymTable<E> implements SymTable<E> {

    private final Map<String, E> table = new HashMap<String, E>();
    private SymTable<E> nextSymTable;

    public HashSymTable() {
        this(null);
    }

    public HashSymTable(SymTable<E> nextSymTable) {
        this.nextSymTable = nextSymTable;
    }

    @Override
    public E lookup(String s) {
        E r = table.get(s);
        if (r != null) {
            return r;
        }
        if (nextSymTable != null) {
            return nextSymTable.lookup(s);
        }
        return null;
    }

    @Override
    public E lookupOnlyInTop(String s) {
        return table.get(s);
    }

    @Override
    public void put(String s, E symbol) {
        table.put(s, symbol);
        System.out.println(symbol + "    "  + s);
    }

    @Override
    public Collection<E> getSymbols() {
        List<E> symbols = new ArrayList<E>();
        symbols.addAll(table.values());
        if (nextSymTable != null) {
            symbols.addAll(nextSymTable.getSymbols());
        }
        return symbols;
    }

    public void printSymbolDetails(){
        List<E> symbols = new ArrayList<E>();
        symbols.addAll(table.values());
        if (nextSymTable != null) {
            symbols.addAll(nextSymTable.getSymbols());
        }
        System.out.println("<++++++++Starting Symbol Logging++++++++>");
        for (int i = 0; i < symbols.size() ;i++){

            if ( symbols.get(i) instanceof  SymTableEntry){
                SymTableEntry asda =  (SymTableEntry)symbols.get(i);
                System.out.println("ID :" + asda.getId());
            }
        }
        return;

    }

    @Override
    public Collection<E> getSymbolsOnlyInTop() {
        List<E> symbols = new ArrayList<E>();
        symbols.addAll(table.values());
        return symbols;
    }

    @Override
    public void clearOnlyInTop() {
        table.clear();
    }

}
