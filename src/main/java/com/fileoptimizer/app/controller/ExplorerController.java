package com.fileoptimizer.app.controller;

import com.fileoptimizer.common.model.FileMetadata;
import com.fileoptimizer.core.analyzer.FileAnalyzer;
import com.fileoptimizer.core.analyzer.FileAnalyzerImpl;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;


public class ExplorerController {

    @FXML private TreeView<Path> folderTree;
    @FXML private TableView<FileMetadata> fileTable;
    @FXML private TableColumn<FileMetadata, String> nameColumn;
    @FXML private TableColumn<FileMetadata, String> sizeColumn;
    @FXML private TableColumn<FileMetadata, String> typeColumn;
    @FXML private TableColumn<FileMetadata, String> dateColumn;
    @FXML private Label currentPathLabel;
    @FXML private TextField searchField;

    private final ObservableList<FileMetadata> fileList = FXCollections.observableArrayList();
    private final FileAnalyzer fileAnalyzer = new FileAnalyzerImpl();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    public void initialize() {
        setupFolderTree();
        setupFileTable();
        setupContextMenu();
        
        // Initial load (Root Drives)
        loadRootDrives();
    }

    private void setupFolderTree() {
        folderTree.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadFiles(newVal.getValue());
            }
        });

        // Custom Cell Factory for Path display
        folderTree.setCellFactory(tv -> new TreeCell<>() {
            @Override
            protected void updateItem(Path item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getFileName() == null ? item.toString() : item.getFileName().toString());
                }
            }
        });
    }

    private void setupFileTable() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        sizeColumn.setCellValueFactory(cellData -> {
            long size = cellData.getValue().getSize();
            return new SimpleStringProperty(formatFileSize(size));
        });

        typeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategory().toString()));

        dateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getModifiedDate() != null) {
                return new SimpleStringProperty(cellData.getValue().getModifiedDate().format(dateFormatter));
            }
            return new SimpleStringProperty("N/A");
        });

        fileTable.setItems(fileList);
    }

    private void setupContextMenu() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem openItem = new MenuItem("Open File");
        openItem.setOnAction(e -> handleOpenFile());

        MenuItem deleteItem = new MenuItem("Delete (Safe)");
        deleteItem.setOnAction(e -> handleDeleteFile());

        MenuItem showInExplorerItem = new MenuItem("Show in Explorer");
        showInExplorerItem.setOnAction(e -> handleShowInExplorer());

        contextMenu.getItems().addAll(openItem, deleteItem, new SeparatorMenuItem(), showInExplorerItem);
        fileTable.setContextMenu(contextMenu);
    }

    private void loadRootDrives() {
        TreeItem<Path> rootItem = new TreeItem<>(Paths.get("Computer"));
        rootItem.setExpanded(true);
        
        for (Path root : FileSystems.getDefault().getRootDirectories()) {
            TreeItem<Path> driveItem = createNode(root);
            rootItem.getChildren().add(driveItem);
        }
        
        folderTree.setRoot(rootItem);
    }

    private TreeItem<Path> createNode(Path path) {
        return new TreeItem<>(path) {
            private boolean isFirstTimeChildren = true;
            private boolean isFirstTimeLeaf = true;
            private boolean isLeaf;

            @Override
            public ObservableList<TreeItem<Path>> getChildren() {
                if (isFirstTimeChildren) {
                    isFirstTimeChildren = false;
                    super.getChildren().setAll(buildChildren(this));
                }
                return super.getChildren();
            }

            @Override
            public boolean isLeaf() {
                if (isFirstTimeLeaf) {
                    isFirstTimeLeaf = false;
                    isLeaf = !Files.isDirectory(getValue());
                }
                return isLeaf;
            }

            private ObservableList<TreeItem<Path>> buildChildren(TreeItem<Path> treeItem) {
                Path path = treeItem.getValue();
                if (path != null && Files.isDirectory(path)) {
                    try (Stream<Path> stream = Files.list(path)) {
                        ObservableList<TreeItem<Path>> children = FXCollections.observableArrayList();
                        stream.filter(Files::isDirectory)
                              .filter(p -> { try { return !Files.isHidden(p); } catch (Exception e) { return false; } })
                              .forEach(p -> children.add(createNode(p)));
                        return children;
                    } catch (IOException e) {
                        return FXCollections.emptyObservableList();
                    }
                }
                return FXCollections.emptyObservableList();
            }
        };
    }

    private void loadFiles(Path folder) {
        if (!Files.isDirectory(folder)) return;
        
        currentPathLabel.setText(folder.toAbsolutePath().toString());
        fileList.clear();

        new Thread(() -> {
            try (Stream<Path> stream = Files.list(folder)) {
                stream.filter(Files::isRegularFile)
                      .forEach(path -> {
                          try {
                              FileMetadata metadata = fileAnalyzer.analyze(path);
                              Platform.runLater(() -> fileList.add(metadata));
                          } catch (IOException ignored) {}
                      });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void handleOpenFile() {
        FileMetadata selected = fileTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                Desktop.getDesktop().open(selected.getPath().toFile());
            } catch (IOException e) {
                showAlert("Error", "Could not open file: " + e.getMessage());
            }
        }
    }

    private void handleDeleteFile() {
        FileMetadata selected = fileTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Move " + selected.getName() + " to Trash?", ButtonType.YES, ButtonType.NO);
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    // Integration with SafeFileDeletionService here
                    fileList.remove(selected);
                }
            });
        }
    }

    private void handleShowInExplorer() {
        FileMetadata selected = fileTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                Runtime.getRuntime().exec("explorer.exe /select," + selected.getPath().toAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.2f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }
}
