/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package pkg409_server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.print.attribute.standard.Severity;
import javax.swing.SwingUtilities;
import jdk.dynalink.linker.ConversionComparator;

/**
 *
 * @author ADMIN
 */
public class MainFormServer extends javax.swing.JFrame {

    ObjectOutputStream out;
    ArrayList<String> s;
    Socket socket;
    /**
     * Creates new form MainFormServer
     */
    public MainFormServer() {
        initComponents();
    }
    
    private void ReceiveMessage()
    {
        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            // Đọc đỉnh nguồn, đỉnh đích và ma trận từ client
            String nguon = in.readUTF();
            String dich = in.readUTF();
            int[][] matrix = (int[][]) in.readObject();

            // Cập nhật giao diện với thông tin nhận được
            History.append("Nhận đỉnh nguồn: " + nguon + "\n");
            History.append("Nhận đỉnh đích: " + dich + "\n");
            History.append("Nhận ma trận:\n");
            for (int[] row : matrix) {
                for (int value : row) {
                    History.append(value + " ");
                }
                History.append("\n");
            }
            int nguon1 = Integer.parseInt(nguon);
            int dich1 = Integer.parseInt(dich);
            Dijkstra(matrix, nguon1, dich1);

        } catch (IOException | ClassNotFoundException e) {
            History.append("Lỗi khi nhận dữ liệu từ client: " + e.getMessage() + "\n");
        }
    }
    
    private void SendMessage(ArrayList<String> s, Result re)
    {
        try {
        out = new ObjectOutputStream(socket.getOutputStream());
        for (String string : s) {
            out.writeObject(string); // Sử dụng writeObject để ghi đối tượng
        }
        out.writeObject("END"); // Gửi "END" như một đối tượng
        out.writeObject(re.path);
        out.writeObject(re.distance);
        out.flush(); // Đảm bảo dữ liệu được gửi ngay lập tức
    } catch (IOException ex) {
        Logger.getLogger(MainFormServer.class.getName()).log(Level.SEVERE, null, ex);
    }
    }
    private void Dijkstra(int [][] graph, int source, int destination)
    {
        int numVertices = graph.length;

        // Mảng lưu khoảng cách ngắn nhất từ đỉnh nguồn đến các đỉnh khác
        int[] shortestDistances = new int[numVertices];

        // Mảng lưu trạng thái đỉnh đã được duyệt hay chưa
        boolean[] visited = new boolean[numVertices];

        // Mảng lưu đỉnh trước đó trên đường đi ngắn nhất
        int[] predecessors = new int[numVertices];

        // Khởi tạo tất cả khoảng cách là vô cực, trừ đỉnh nguồn
        Arrays.fill(shortestDistances, Integer.MAX_VALUE);
        Arrays.fill(predecessors, -1); // -1 nghĩa là không có đỉnh trước đó
        shortestDistances[source] = 0;

        // Lặp qua tất cả các đỉnh
        for (int count = 0; count < numVertices - 1; count++) {
            // Lấy đỉnh có khoảng cách ngắn nhất chưa được duyệt
            int u = minDistance(shortestDistances, visited);

            // Đánh dấu đỉnh này đã được duyệt
            visited[u] = true;

            // Cập nhật khoảng cách của các đỉnh kề của đỉnh đã chọn
            for (int v = 0; v < numVertices; v++) {
                // Chỉ cập nhật nếu có đường đi từ u tới v và v chưa được duyệt
                // Và nếu đường đi qua u là ngắn hơn đường đi hiện tại tới v
                if (!visited[v] && graph[u][v] != 0 &&
                        shortestDistances[u] != Integer.MAX_VALUE &&
                        shortestDistances[u] + graph[u][v] < shortestDistances[v]) {

                    shortestDistances[v] = shortestDistances[u] + graph[u][v];
                    predecessors[v] = u; // Lưu đỉnh trước đó
                }
            }
        }

        // In kết quả khoảng cách từ nguồn đến tất cả các đỉnh và đường đi
        ArrayList<String> results = new ArrayList<>();
        for (int i = 0; i < shortestDistances.length; i++) {
            if (i != source) {
                String path = printPath(predecessors, i); // Gọi hàm để lấy đường đi
                History.append(path + "\n");
                results.add(path); // Thêm đường đi vào danh sách kết quả
            }
        }

        // Lấy kết quả cho đỉnh đích mong muốn
        Result re = new Result("", 0);
        if (shortestDistances[destination] == Integer.MAX_VALUE) {
            String noPathMessage = "Không có đường đi từ " + source + " đến " + destination;
            History.append(noPathMessage + "\n");
            re = new Result(noPathMessage, 0);
        } else {
            String destinationPath = printPath(predecessors, destination);
            String destinationResult = "Đường đi ngắn nhất từ " + source + " đến " + destination + ": " + destinationPath + " với khoảng cách: " + shortestDistances[destination];
            History.append(destinationResult + "\n");
            re = new Result(destinationPath, shortestDistances[destination]);
        }
        SendMessage(results, re);
    }

    //Hàm tìm đỉnh kề gần nhất
    private int minDistance(int[] distances, boolean[] visited) {
        int min = Integer.MAX_VALUE, minIndex = -1;

        for (int v = 0; v < distances.length; v++) {
            if (!visited[v] && distances[v] <= min) {
                min = distances[v];
                minIndex = v;
            }
        }

        return minIndex;
    }
    
    //Hàm trả về đường đi từ 1 đỉnh tới 1 đỉnh
    private String printPath(int[] predecessors, int currentVertex) {
        if (predecessors[currentVertex] == -1) {
            return String.valueOf(currentVertex);
        }
        return printPath(predecessors, predecessors[currentVertex]) + " -> " + currentVertex;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bt_Start = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        History = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        bt_Start.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        bt_Start.setForeground(java.awt.SystemColor.activeCaption);
        bt_Start.setText("Start");
        bt_Start.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bt_StartActionPerformed(evt);
            }
        });

        History.setColumns(20);
        History.setRows(5);
        jScrollPane1.setViewportView(History);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(32, 32, 32)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(155, 155, 155)
                        .addComponent(bt_Start)))
                .addContainerGap(36, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(bt_Start)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(26, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void bt_StartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bt_StartActionPerformed
        // Thêm thông báo chờ kết nối
    History.append("Đang đợi kết nối...\n");

    // Tạo một luồng mới để chạy server
    new Thread(() -> {
        try {
            ServerSocket svSocket = new ServerSocket(8888);
            while (true) {
                try {
                    // Chấp nhận kết nối từ client
                    socket = svSocket.accept();

                    // Cập nhật giao diện trong Event Dispatch Thread
                    SwingUtilities.invokeLater(() -> History.append("Kết nối thành công!\n"));
                    ReceiveMessage();

                } catch (IOException e) {
                    SwingUtilities.invokeLater(() -> History.append("Lỗi khi chấp nhận kết nối: " + e.getMessage() + "\n"));
                }
            }
        } catch (IOException ex) {
            SwingUtilities.invokeLater(() -> History.append("Đã xảy ra lỗi khi tạo ServerSocket: " + ex.getMessage() + "\n"));
        }
    }).start(); // Bắt đầu luồng server
        
    }//GEN-LAST:event_bt_StartActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainFormServer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainFormServer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainFormServer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainFormServer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainFormServer().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea History;
    private javax.swing.JButton bt_Start;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
