
import java.util.PriorityQueue;
import java.util.Stack;

class Vertex {

    public Tile label;
    public boolean wasVisited;
    public boolean discoverd = false;

    public Vertex(Tile lab) {
        label = lab;
        wasVisited = false;
    }

    public static int encode(Tile t) {
        if (t.getRow() % Mesh.VIEW_rad != 0 || t.getCol() % Mesh.VIEW_rad != 0) {
            throw new UnsupportedOperationException("invalid tile " + t);
        }
        return (t.getRow() / Mesh.VIEW_rad) * Mesh.MESH_width + (t.getCol() / Mesh.VIEW_rad);
    }

    public static Tile decode(int code) {
        return new Tile((code / Mesh.MESH_width) * Mesh.VIEW_rad, (code % Mesh.MESH_width) * Mesh.VIEW_rad);
    }
}

public class Mesh {

    public static Ants ants;
    public static int MESH_height, MESH_width, VIEW_rad;
    private int MAX_VERTS;
    private Vertex vertexList[];
    private int adjMat[][];
    private Stack<Integer> theStack;
    private PriorityQueue<Integer> theQueue;

    public Mesh(Ants ants) {
        Mesh.ants = ants;
        VIEW_rad = (int) Math.sqrt(ants.getViewRadius2());
        MESH_height = ants.getRows() / VIEW_rad + 1;
        MESH_width = ants.getCols() / VIEW_rad + 1;
        MAX_VERTS = MESH_height * MESH_width;
        vertexList = new Vertex[MAX_VERTS];
        adjMat = new int[MAX_VERTS][MAX_VERTS];
        for (int j = 0; j < MAX_VERTS; j++) {
            for (int k = 0; k < MAX_VERTS; k++) {
                adjMat[j][k] = 0;
            }
        }
        for (int i = 0; i < MAX_VERTS; i++) {
            vertexList[i] = new Vertex(Vertex.decode(i));
        }
        theStack = new Stack<Integer>();
        theQueue = new PriorityQueue<Integer>();

    }

    public void addVertex(Tile lab) {
        vertexList[Vertex.encode(lab)] = new Vertex(lab);
    }

    public void addEdge(Tile start, Tile end) {
        adjMat[Vertex.encode(start)][Vertex.encode(end)] = 1;
        adjMat[Vertex.encode(end)][Vertex.encode(start)] = 1;
    }

    public void displayVertex(Tile v) {
        MyBot.debugln(vertexList[Vertex.encode(v)].label.toString());
    }

    public void displayVertex(int v) {
        MyBot.debugln(vertexList[v].label.toString());
    }

    public void dfs(Tile startTile) {
        int start = Vertex.encode(startTile);
        vertexList[start].wasVisited = true;
        displayVertex(startTile);
        theStack.push(start);
        while (!theStack.isEmpty()) {
            int v = getAdjUnvisitedVertex((Integer) theStack.peek());
            if (v == -1) {
                theStack.pop();
            } else {
                vertexList[v].wasVisited = true;
                displayVertex(v);
                theStack.push(v);
            }
        }
        for (int j = 0; j < MAX_VERTS; j++) {
            vertexList[j].wasVisited = false;
        }
    }

    public void displayMesh() {
        MyBot.debugln("Map Rows " + ants.getRows() + " Mesh height " + MESH_height);
        MyBot.debugln("Map Cols " + ants.getCols() + " Mesh width " + MESH_width);
        MyBot.debugln("View Radius " + VIEW_rad + " " + ants.getViewRadius2());
        for (int i = 0; i < MAX_VERTS; i++) {
            MyBot.debug(Vertex.decode(i).toString() + " " + i + "\t");

        }
        MyBot.debugln("");
        for (int i = 0; i < MAX_VERTS; i++) {
            for (int j = 0; j < MAX_VERTS; j++) {
                MyBot.debug(adjMat[i][j] + "\t");
            }
            MyBot.debugln("");
        }
    }

    public void bfs(int start) {
        vertexList[start].wasVisited = true;
        displayVertex(start);
        theQueue.add(new Integer(start));
        int v2;
        while (!theQueue.isEmpty()) {
            int v1 = theQueue.poll();
            while ((v2 = getAdjUnvisitedVertex(v1)) != -1 && vertexList[v2].wasVisited == false) {
                vertexList[v2].wasVisited = true;
                displayVertex(v2);
                theQueue.add(new Integer(v2));
            }
        }
        for (int j = 0; j < MAX_VERTS; j++) {
            vertexList[j].wasVisited = false;
        }
    }

    public void mst(int start) {
        vertexList[start].wasVisited = true;
        theStack.push(start);
        while (!theStack.isEmpty()) {
            int currentVertex = (Integer) theStack.peek();
            int v = getAdjUnvisitedVertex(currentVertex);
            if (v == -1) {
                theStack.pop();
            } else {
                vertexList[v].wasVisited = true;
                theStack.push(v);
                displayVertex(currentVertex);
                displayVertex(v);
                System.out.print(" ");
            }
        }
        for (int j = 0; j < MAX_VERTS; j++) {
            vertexList[j].wasVisited = false;
        }
    }

    public int getAdjUnvisitedVertex(int v) {
        for (int j = 0; j < MAX_VERTS; j++) {
            if (adjMat[v][j] == 1 && vertexList[j].wasVisited == false) {
                return j;
            }
        }
        return -1;
    }

    public void updateMesh() {
    }
}
//
//class GraphSearch {
//
//    public static void main(String[] args) {
//        Graph theGraph = new Graph();
//        theGraph.addVertex('A');
//        theGraph.addVertex('B');
//        theGraph.addVertex('C');
//        theGraph.addVertex('D');
//        theGraph.addVertex('E');
//        theGraph.addEdge(0, 1); // AB
//        theGraph.addEdge(0, 2); // AC
//        theGraph.addEdge(0, 3); // AD
//        theGraph.addEdge(0, 4); // AE
//        theGraph.addEdge(1, 2); // BC
//        theGraph.addEdge(1, 3); // BD
//        theGraph.addEdge(1, 4); // BE
//        theGraph.addEdge(2, 3); // CD
//        theGraph.addEdge(2, 4); // CE
//        theGraph.addEdge(3, 4); // DE
//        System.out.print("Visits: ");
//        theGraph.dfs(1);
//        System.out.println();
//        System.out.print("Visits: ");
//        theGraph.bfs(1);
//        System.out.println();
//        System.out.print("Minimum Spanning Tree:");
//        theGraph.mst(1);
//        System.out.println();
//    }
//}
