/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pkg409_server;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ADMIN
 */
public class Graph {

    private final int vertices; // Số lượng đỉnh trong đồ thị
    private final List<List<Node>> adjacencyList; // Danh sách kề cho đồ thị

    // Khởi tạo đồ thị với số đỉnh
    public Graph(int vertices) {
        this.vertices = vertices;
        adjacencyList = new ArrayList<>();
        for (int i = 0; i < vertices; i++) {
            adjacencyList.add(new ArrayList<>());
        }
    }
}
