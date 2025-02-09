/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package presentacion;

import java.awt.event.KeyEvent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import negocio.VentaControl;

/**
 *
 * @author PC
 */
public class FrmVenta extends javax.swing.JInternalFrame {
    
    private final VentaControl CONTROL;
    private String accion;
    private String nombreAnt;
    
    private int totalPorPagina = 10;
    private int numPagina = 1;
    private boolean primeraCarga = true;
    private int totalRegistros;
    
    public DefaultTableModel modeloDetalles;
    public JFrame contenedor;

    
    /**
     * Creates new form FrmCategoria
     */
    public FrmVenta(JFrame frmP) {
        initComponents();
        this.contenedor=frmP;
        this.CONTROL=new VentaControl();
        this.paginar();
        this.listar("",false);
        this.primeraCarga=false;
        tabGeneral.setEnabledAt(1, false);
        this.accion="guardar";
        this.crearDetalles();
    }
    
    private void paginar(){
        int totalPaginas;
        
        this.totalRegistros=this.CONTROL.total();
        this.totalPorPagina=Integer.parseInt((String)cboTotalPorPagina.getSelectedItem());
        totalPaginas=(int)(Math.ceil((double)this.totalRegistros/this.totalPorPagina));
        if (totalPaginas==0){
            totalPaginas=1;
        }
        cboNumPagina.removeAllItems();
        
        for (int i = 1; i <= totalPaginas; i++) {
            cboNumPagina.addItem(Integer.toString(i));
        }
        cboNumPagina.setSelectedIndex(0);
    }
    
    private void listar(String texto, boolean paginar){
        this.totalPorPagina=Integer.parseInt((String)cboTotalPorPagina.getSelectedItem());
        if ((String)cboNumPagina.getSelectedItem()!=null){
            this.numPagina=Integer.parseInt((String)cboNumPagina.getSelectedItem());
        }
        
        if (paginar==true){
            tablaListado.setModel(this.CONTROL.listar(texto,this.totalPorPagina,this.numPagina));
        }else{
            tablaListado.setModel(this.CONTROL.listar(texto,this.totalPorPagina,1));
        }
        
        
        TableRowSorter orden= new TableRowSorter(tablaListado.getModel());
        tablaListado.setRowSorter(orden);
        this.ocultarColumnas();
        lblTotalRegistros.setText("Mostrando " + this.CONTROL.totalMostrados() + " de un total de " + this.CONTROL.total() + " registros");
    }
    
    private void ocultarColumnas(){
        tablaListado.getColumnModel().getColumn(1).setMaxWidth(0);
        tablaListado.getColumnModel().getColumn(1).setMinWidth(0);
        tablaListado.getTableHeader().getColumnModel().getColumn(1).setMaxWidth(0);
        tablaListado.getTableHeader().getColumnModel().getColumn(1).setMinWidth(0);
        
        tablaListado.getColumnModel().getColumn(3).setMaxWidth(0);
        tablaListado.getColumnModel().getColumn(3).setMinWidth(0);
        tablaListado.getTableHeader().getColumnModel().getColumn(3).setMaxWidth(0);
        tablaListado.getTableHeader().getColumnModel().getColumn(3).setMinWidth(0);
    }
    
    private void crearDetalles(){
        modeloDetalles = new DefaultTableModel(){
            @Override
            public boolean isCellEditable(int fila, int columna) { 
                if (columna==4){
                    return columna==4;
                }
                if (columna==5){
                    return columna==5;
                }
                if (columna==6){
                    return columna==6;
                }
                return columna==4;
            }

            @Override
            public Object getValueAt(int row, int col) {
                if (col==7){
                    int cantD;
                    try {
                        cantD=Integer.parseInt((String)getValueAt(row, 4));
                    } catch (Exception e) {
                        cantD=1;
                    }
                    int precioD=Integer.parseInt((String)getValueAt(row, 5));
                    int descuentoD=Integer.parseInt((String)getValueAt(row, 6));
                    if (cantD!=0 && precioD!=0){
                        return ((cantD*precioD)-descuentoD);
                    }else{
                        return 0;
                    }
                }
                return super.getValueAt(row, col);
            }


            @Override
            public void setValueAt(Object aValue, int row, int col) {
                super.setValueAt(aValue, row, col);
                try {
                    int cantD=Integer.parseInt((String)getValueAt(row, 4));
                    int stockD=Integer.parseInt((String)getValueAt(row, 3));
                    if (cantD>stockD){
                        super.setValueAt(stockD, row, 4);
                        mensajeError("La cantidad a vender no puede superar el stock. usted puede vender como máximo: " + stockD);
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                calcularTotales();
                fireTableDataChanged();
            }
        
        };
        
        modeloDetalles.setColumnIdentifiers(new Object[]{"Id","CODIGO","ARTICULO","STOCK","CANTIDAD","PRECIO","DESCUENTO","SUBTOTAL"});
        tablaDetalles.setModel(modeloDetalles);
    }
    
    public void agregarDetalles(String id,String codigo, String nombre,String stock,String precio,String descuento){
        String idT;
        boolean existe=false;
        for (int i = 0; i < this.modeloDetalles.getRowCount(); i++) {
            idT=String.valueOf(this.modeloDetalles.getValueAt(i, 0));
            if (idT.equals(id)){
                existe=true;
            }
        }
        if (existe){
            this.mensajeError("El artículo ya ha sido agregado.");
        }
        else{
            this.modeloDetalles.addRow(new Object[]{id,codigo,nombre,stock,"1",precio,descuento,precio});
            this.calcularTotales();
        }
    }
    
    private void calcularTotales(){
        int total=0;
        int subTotal;
        int items=modeloDetalles.getRowCount();
        if (items==0){
            total=0;
        }else{
            for (int i = 0; i < items; i++) {
                total=total+Integer.parseInt(String.valueOf(modeloDetalles.getValueAt(i, 7)));
            }
        }
        subTotal=(total*(Integer.parseInt(txtImpuesto.getText())))/100;
        txtTotal.setText(String.format("%d", total+subTotal));
        txtSubTotal.setText(String.format("%d",total));
        txtTotalImpuesto.setText(String.format("%d",subTotal));
    }
       
    private void limpiar(){
        txtNombreCliente.setText("");
        txtIdCliente.setText("");
        txtSerieComprobante.setText("");
        txtNumComprobante.setText("");
        txtImpuesto.setText("0");//19 para iva chile

        this.accion="guardar";
        
        txtTotal.setText("0");
        txtSubTotal.setText("0");
        txtTotalImpuesto.setText("0");      
        this.crearDetalles();
        btnGuardar.setVisible(true);
    }
    
    private void mensajeError(String mensaje){
        JOptionPane.showMessageDialog(this, mensaje,"Sistema",JOptionPane.ERROR_MESSAGE);
    }
    
    private void mensajeOk(String mensaje){
        JOptionPane.showMessageDialog(this, mensaje,"Sistema",JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabGeneral = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtBuscar = new javax.swing.JTextField();
        btnBuscar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tablaListado = new javax.swing.JTable();
        lblTotalRegistros = new javax.swing.JLabel();
        btnNuevo = new javax.swing.JButton();
        btnDesactivar = new javax.swing.JButton();
        cboNumPagina = new javax.swing.JComboBox<>();
        cboTotalPorPagina = new javax.swing.JComboBox<>();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        btnVerVenta = new javax.swing.JButton();
        btnReporteComprobante = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        btnGuardar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        txtIdCliente = new javax.swing.JTextField();
        txtNombreCliente = new javax.swing.JTextField();
        btnSeleccionarCliente = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        txtImpuesto = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        cboTipoComprobante = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        txtSerieComprobante = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtNumComprobante = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        txtCodigo = new javax.swing.JTextField();
        btnVerArticulos = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tablaDetalles = new javax.swing.JTable();
        jLabel9 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        txtSubTotal = new javax.swing.JTextField();
        txtTotalImpuesto = new javax.swing.JTextField();
        txtTotal = new javax.swing.JTextField();
        btnQuitar = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Ventas");

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setText("Nombre:");

        btnBuscar.setText("Buscar");
        btnBuscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarActionPerformed(evt);
            }
        });

        tablaListado.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        tablaListado.setShowGrid(false);
        jScrollPane1.setViewportView(tablaListado);

        lblTotalRegistros.setText("Registros");

        btnNuevo.setText("Nuevo");
        btnNuevo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevoActionPerformed(evt);
            }
        });

        btnDesactivar.setText("Anular");
        btnDesactivar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDesactivarActionPerformed(evt);
            }
        });

        cboNumPagina.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboNumPaginaActionPerformed(evt);
            }
        });

        cboTotalPorPagina.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "10", "20", "50", "100", "200", "500" }));
        cboTotalPorPagina.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboTotalPorPaginaActionPerformed(evt);
            }
        });

        jLabel10.setText("# Página");

        jLabel11.setText("Total de registros por página ");

        btnVerVenta.setText("Ver Venta");
        btnVerVenta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVerVentaActionPerformed(evt);
            }
        });

        btnReporteComprobante.setText("Reporte");
        btnReporteComprobante.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReporteComprobanteActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addGap(18, 18, 18)
                                .addComponent(txtBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 285, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnNuevo, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 70, Short.MAX_VALUE)
                                .addComponent(btnReporteComprobante, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnVerVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel10)
                                .addGap(47, 47, 47)
                                .addComponent(cboNumPagina, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(88, 88, 88)
                                .addComponent(jLabel11)
                                .addGap(35, 35, 35)
                                .addComponent(cboTotalPorPagina, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnDesactivar, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblTotalRegistros, javax.swing.GroupLayout.PREFERRED_SIZE, 231, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(70, 70, 70))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBuscar)
                    .addComponent(btnNuevo)
                    .addComponent(btnVerVenta)
                    .addComponent(btnReporteComprobante))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 394, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cboNumPagina, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboTotalPorPagina, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10)
                    .addComponent(jLabel11))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnDesactivar)
                    .addComponent(lblTotalRegistros, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(31, Short.MAX_VALUE))
        );

        tabGeneral.addTab("Listado", jPanel1);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        jLabel2.setText("Cliente (*)");

        btnGuardar.setText("Guardar");
        btnGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarActionPerformed(evt);
            }
        });

        btnCancelar.setText("Cancelar");
        btnCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarActionPerformed(evt);
            }
        });

        jLabel4.setText("(*) Indica que es un campo obligatorio.");

        txtIdCliente.setEditable(false);

        txtNombreCliente.setEditable(false);

        btnSeleccionarCliente.setText("...");
        btnSeleccionarCliente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSeleccionarClienteActionPerformed(evt);
            }
        });

        jLabel3.setText("Impuesto (*)");

        txtImpuesto.setText("0");
        txtImpuesto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtImpuestoActionPerformed(evt);
            }
        });

        jLabel5.setText("Tipo Comprobante (*)");

        cboTipoComprobante.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "BOLETA", "FACTURA" }));
        cboTipoComprobante.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboTipoComprobanteActionPerformed(evt);
            }
        });

        jLabel6.setText("Serie Comprobante");

        jLabel7.setText("Número Comprobante (*):");

        jLabel8.setText("Artículo");

        txtCodigo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCodigoActionPerformed(evt);
            }
        });
        txtCodigo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtCodigoKeyReleased(evt);
            }
        });

        btnVerArticulos.setText("Ver");
        btnVerArticulos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVerArticulosActionPerformed(evt);
            }
        });

        tablaDetalles.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane2.setViewportView(tablaDetalles);

        jLabel9.setText("Subtotal");

        jLabel12.setText("Total Impuesto");

        jLabel13.setText("Total");

        txtSubTotal.setEditable(false);

        txtTotalImpuesto.setEditable(false);

        txtTotal.setEditable(false);

        btnQuitar.setText("Quitar");
        btnQuitar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuitarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(39, 39, 39)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel4)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(btnGuardar)
                                        .addGap(18, 18, 18)
                                        .addComponent(btnCancelar)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel13))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel12)
                                    .addComponent(jLabel9))))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtSubTotal)
                            .addComponent(txtTotalImpuesto)
                            .addComponent(txtTotal, javax.swing.GroupLayout.DEFAULT_SIZE, 82, Short.MAX_VALUE)))
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addComponent(jLabel2)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(txtIdCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(txtNombreCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(btnSeleccionarCliente)
                            .addGap(145, 145, 145)
                            .addComponent(jLabel3)
                            .addGap(18, 18, 18)
                            .addComponent(txtImpuesto, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addComponent(jLabel5)
                            .addGap(18, 18, 18)
                            .addComponent(cboTipoComprobante, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(20, 20, 20)
                            .addComponent(jLabel6)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(txtSerieComprobante, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(jLabel7)
                            .addGap(18, 18, 18)
                            .addComponent(txtNumComprobante, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addComponent(jLabel8)
                            .addGap(18, 18, 18)
                            .addComponent(txtCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, 253, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(btnVerArticulos)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnQuitar))
                        .addComponent(jScrollPane2)))
                .addContainerGap(101, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtImpuesto, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel2)
                        .addComponent(txtIdCliente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtNombreCliente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnSeleccionarCliente)
                        .addComponent(jLabel3)))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(cboTipoComprobante, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(txtSerieComprobante, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(txtNumComprobante, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(31, 31, 31)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(txtCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnVerArticulos)
                    .addComponent(btnQuitar))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 296, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtSubTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtTotalImpuesto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel13)
                            .addComponent(txtTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 14, Short.MAX_VALUE)
                        .addComponent(jLabel4)
                        .addGap(19, 19, 19)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnGuardar)
                            .addComponent(btnCancelar))
                        .addContainerGap())))
        );

        tabGeneral.addTab("Mantenimiento", jPanel2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabGeneral)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabGeneral)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarActionPerformed
        this.listar(txtBuscar.getText(),false);
    }//GEN-LAST:event_btnBuscarActionPerformed

    private void btnNuevoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoActionPerformed
        tabGeneral.setEnabledAt(1, true);
        tabGeneral.setEnabledAt(0, false);
        tabGeneral.setSelectedIndex(1);
        this.accion="guardar";
        btnGuardar.setText("Guardar");
        this.obtenerNumero();
    }//GEN-LAST:event_btnNuevoActionPerformed

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
        tabGeneral.setEnabledAt(0, true);
        tabGeneral.setEnabledAt(1, false);
        tabGeneral.setSelectedIndex(0);
        this.limpiar();
    }//GEN-LAST:event_btnCancelarActionPerformed

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        if (txtIdCliente.getText().length()==0){
            JOptionPane.showMessageDialog(this, "Debes seleccionar un Cliente.","Sistema", JOptionPane.WARNING_MESSAGE);
            btnSeleccionarCliente.requestFocus();
            return;
        }
        if (txtSerieComprobante.getText().length()>7){
            JOptionPane.showMessageDialog(this, "Debes ingresar una serie no mayor a 7 caracteres.","Sistema", JOptionPane.WARNING_MESSAGE);
            txtSerieComprobante.requestFocus();
            return;
        }
        if (txtNumComprobante.getText().length()==0 || txtNumComprobante.getText().length()>10){
            JOptionPane.showMessageDialog(this, "Debes ingresar un número de comprobante no mayor a 10 caracteres.","Sistema", JOptionPane.WARNING_MESSAGE);
            txtNumComprobante.requestFocus();
            return;
        }  
        if (modeloDetalles.getRowCount()==0){
            JOptionPane.showMessageDialog(this, "Debes agregar artículos al detalle.","Sistema", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String resp="";
        resp=this.CONTROL.insertar(Integer.parseInt(txtIdCliente.getText()),(String)cboTipoComprobante.getSelectedItem(),txtSerieComprobante.getText(),txtNumComprobante.getText(),Integer.parseInt(txtTotalImpuesto.getText()),Integer.parseInt(txtTotal.getText()),modeloDetalles);
        if (resp.equals("OK")) {
            this.mensajeOk("Registrado correctamente");
            this.limpiar();
            this.listar("", false);
            /*tabGeneral.setSelectedIndex(0);
                tabGeneral.setEnabledAt(1, false);
                tabGeneral.setEnabledAt(0, true);*/
        } else {
            this.mensajeError(resp);
        }       
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void btnDesactivarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDesactivarActionPerformed
        if (tablaListado.getSelectedRowCount() == 1) {
            String id= String.valueOf(tablaListado.getValueAt(tablaListado.getSelectedRow(),0));
            String comprobante= String.valueOf(tablaListado.getValueAt(tablaListado.getSelectedRow(),5));
            String serie= String.valueOf(tablaListado.getValueAt(tablaListado.getSelectedRow(),6));
            String numero= String.valueOf(tablaListado.getValueAt(tablaListado.getSelectedRow(),7));
            
            
            if(JOptionPane.showConfirmDialog(this,"Deseas anular el registro: " + comprobante+ " " + serie+ "-"+numero + " ?", "Anular", JOptionPane.YES_NO_OPTION)==0){
                String resp=this.CONTROL.anular(Integer.parseInt(id));
                if (resp.equals("OK")){
                    this.mensajeOk("Registro anulado");
                    this.listar("",false);
                }else{
                    this.mensajeError(resp);
                }
            }
        } else {
            this.mensajeError("Seleccione 1 registro a desactivar.");
        }
    }//GEN-LAST:event_btnDesactivarActionPerformed

    private void cboNumPaginaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboNumPaginaActionPerformed
        if (this.primeraCarga==false){
            this.listar("",true);
        }
    }//GEN-LAST:event_cboNumPaginaActionPerformed

    private void cboTotalPorPaginaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboTotalPorPaginaActionPerformed
        this.paginar();
    }//GEN-LAST:event_cboTotalPorPaginaActionPerformed

    private void btnSeleccionarClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSeleccionarClienteActionPerformed
        FrmSeleccionarClienteVenta frm=new FrmSeleccionarClienteVenta(contenedor, this, true);
        frm.toFront();
    }//GEN-LAST:event_btnSeleccionarClienteActionPerformed

    private void btnVerArticulosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVerArticulosActionPerformed
        FrmSeleccionarArticuloVenta frm =new FrmSeleccionarArticuloVenta(contenedor, this, true);
        frm.toFront();
    }//GEN-LAST:event_btnVerArticulosActionPerformed

    private void txtCodigoKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCodigoKeyReleased
        if (txtCodigo.getText().length()>0){
            if (evt.getKeyCode()==KeyEvent.VK_ENTER){
                entidades.Articulo art;
                art=this.CONTROL.obtenerArticuloCodigoVenta(txtCodigo.getText());
                if (art==null){
                    this.mensajeError("No existe un artículo con ese código");
                }else{
                    this.agregarDetalles(Integer.toString(art.getId()),art.getCodigo(),art.getNombre(),Integer.toString(art.getStock()),Integer.toString(art.getPrecioVenta()),"0");
                }
            }
            
        }
    }//GEN-LAST:event_txtCodigoKeyReleased

    private void btnQuitarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuitarActionPerformed
        if (tablaDetalles.getSelectedRowCount() == 1) {
            this.modeloDetalles.removeRow(tablaDetalles.getSelectedRow());
            this.calcularTotales();
        }else{
            this.mensajeError("Seleccione el detalle a quitar.");
        }
    }//GEN-LAST:event_btnQuitarActionPerformed

    private void btnVerVentaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVerVentaActionPerformed
        if (tablaListado.getSelectedRowCount()==1){
            String id= String.valueOf(tablaListado.getValueAt(tablaListado.getSelectedRow(),0));
            String idProveedor= String.valueOf(tablaListado.getValueAt(tablaListado.getSelectedRow(),3));
            String nombreProveedor= String.valueOf(tablaListado.getValueAt(tablaListado.getSelectedRow(),4));            
            String tipoComprobante=String.valueOf(tablaListado.getValueAt(tablaListado.getSelectedRow(),5));
            String serie = String.valueOf(tablaListado.getValueAt(tablaListado.getSelectedRow(),6));
            String numero=String.valueOf(tablaListado.getValueAt(tablaListado.getSelectedRow(),7));
            String impuesto=String.valueOf(tablaListado.getValueAt(tablaListado.getSelectedRow(),9));
            
            txtIdCliente.setText(idProveedor);
            txtNombreCliente.setText(nombreProveedor);            
            cboTipoComprobante.setSelectedItem(tipoComprobante);
            txtSerieComprobante.setText(serie);
            txtNumComprobante.setText(numero); 
            txtImpuesto.setText(impuesto);
            
            this.modeloDetalles=CONTROL.listarDetalle(Integer.parseInt(id));
            tablaDetalles.setModel(modeloDetalles);
            this.calcularTotales();
            
            tabGeneral.setEnabledAt(1, true);
            tabGeneral.setEnabledAt(0, false);
            tabGeneral.setSelectedIndex(1);
            btnGuardar.setVisible(false);
        }else{
           this.mensajeError("Seleccione la venta a mostrar."); 
        }
    }//GEN-LAST:event_btnVerVentaActionPerformed

    private void obtenerNumero() {
        String tipoComprobante = (String) cboTipoComprobante.getSelectedItem();
        String serieComprobante = this.CONTROL.ultimoSerie(tipoComprobante);
        String numComprobante = this.CONTROL.ultimoNumero(tipoComprobante, serieComprobante);
        txtSerieComprobante.setText(serieComprobante);
        if (numComprobante.equals("")){
            txtNumComprobante.setText("");
        }else{
            int num;
            num=Integer.parseInt(numComprobante)+1;
            txtNumComprobante.setText(Integer.toString(num));
        }        
    }
    
    private void btnReporteComprobanteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReporteComprobanteActionPerformed
        if (tablaListado.getSelectedRowCount()==1){
            String id= String.valueOf(tablaListado.getValueAt(tablaListado.getSelectedRow(),0));
            this.CONTROL.reporteComprobante(id);
        }else{
           this.mensajeError("Seleccione la venta para ver su reporte."); 
        }
    }//GEN-LAST:event_btnReporteComprobanteActionPerformed

    private void cboTipoComprobanteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboTipoComprobanteActionPerformed
        this.obtenerNumero();
    }//GEN-LAST:event_cboTipoComprobanteActionPerformed

    private void txtImpuestoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtImpuestoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtImpuestoActionPerformed

    private void txtCodigoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCodigoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCodigoActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnDesactivar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnNuevo;
    private javax.swing.JButton btnQuitar;
    private javax.swing.JButton btnReporteComprobante;
    private javax.swing.JButton btnSeleccionarCliente;
    private javax.swing.JButton btnVerArticulos;
    private javax.swing.JButton btnVerVenta;
    private javax.swing.JComboBox<String> cboNumPagina;
    private javax.swing.JComboBox<String> cboTipoComprobante;
    private javax.swing.JComboBox<String> cboTotalPorPagina;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblTotalRegistros;
    private javax.swing.JTabbedPane tabGeneral;
    private javax.swing.JTable tablaDetalles;
    private javax.swing.JTable tablaListado;
    private javax.swing.JTextField txtBuscar;
    private javax.swing.JTextField txtCodigo;
    public javax.swing.JTextField txtIdCliente;
    private javax.swing.JTextField txtImpuesto;
    public javax.swing.JTextField txtNombreCliente;
    private javax.swing.JTextField txtNumComprobante;
    private javax.swing.JTextField txtSerieComprobante;
    private javax.swing.JTextField txtSubTotal;
    private javax.swing.JTextField txtTotal;
    private javax.swing.JTextField txtTotalImpuesto;
    // End of variables declaration//GEN-END:variables
}
