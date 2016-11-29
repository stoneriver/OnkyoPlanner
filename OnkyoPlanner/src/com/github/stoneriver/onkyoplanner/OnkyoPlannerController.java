/*Copyright 2016 stoneriver
 *
 *Licensed under the Apache License, Version 2.0 (the "License");
 *you may not use this file except in compliance with the License.
 *You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing, software
 *distributed under the License is distributed on an "AS IS" BASIS,
 *WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *See the License for the specific language governing permissions and
 *limitations under the License.
 */
package com.github.stoneriver.onkyoplanner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

import com.github.stoneriver.onkyohelper.Event;
import com.github.stoneriver.onkyohelper.OnkyoHelperCollections;
import com.github.stoneriver.onkyohelper.Plan;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;

public class OnkyoPlannerController implements Initializable {
	
	//変数宣言
	//プラン・ファイル
	private Plan plan;
	private File file;
	
	//イベントリスト
	private ObservableList<Event> eventList;
	@FXML
	private TableView<Event> tableView;
	@FXML
	private TableColumn<Event, Integer> columnEventNum;
	@FXML
	private TableColumn<Event, String> columnEventName;
	@FXML
	private TableColumn<Event, Integer> columnEventStart;
	
	//イベント追加
	@FXML
	private TextField fieldNewEventName;
	@FXML
	private TextField fieldNewEventStart;
	@FXML
	private Button buttonGenerateEvent;
	
	//メニュー
	@FXML
	private MenuItem menuItemExport;
	@FXML
	private Menu menuOpen;
	private MenuItem menuItemUpdate;
	private MenuItem menuItemNew;
	
	//関数宣言
	//初期化関数
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		initializeEventList();
		initializeNewEventField();
		initializeMenuUpdate();
		initializeMenuOpen();
		onMenuItemUpdateAction();
		setDisableAll(true);
	}
	
	private void initializeMenuOpen() {
		menuItemNew = new MenuItem(Messages.getString("OnkyoPlannerController.New")); //$NON-NLS-1$
		menuItemNew.setOnAction((ActionEvent e) -> {
			onMenuItemNewAction();
		});
	}
	
	private void initializeMenuUpdate() {
		menuItemUpdate = new MenuItem(Messages.getString("OnkyoPlannerController.Update")); //$NON-NLS-1$
		menuItemUpdate.setOnAction((ActionEvent e) -> {
			onMenuItemUpdateAction();
		});
	}
	
	private void initializeEventList() {
		eventList = FXCollections.observableArrayList();
		columnEventNum.setCellValueFactory(new PropertyValueFactory<>("Num")); //$NON-NLS-1$
		columnEventName.setCellValueFactory(new PropertyValueFactory<>("Name")); //$NON-NLS-1$
		columnEventName.setCellFactory(TextFieldTableCell.<Event> forTableColumn());
		columnEventStart.setCellValueFactory(new PropertyValueFactory<>("Start")); //$NON-NLS-1$
		tableView.setItems(eventList);
		tableView.setEditable(true);
	}
	
	private void initializeNewEventField() {
		fieldNewEventName.setText(""); //$NON-NLS-1$
		fieldNewEventStart.setText(""); //$NON-NLS-1$
	}
	
	//クラス関数
	private void loadPlan(String property) {
		try {
			file = new File(property);
			plan = new Plan(property, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		eventList.addAll(plan.getEvents());
		setDisableAll(false);
	}
	
	private Control[] getAllControls() {
		Control[] result = {
				tableView,
				fieldNewEventName,
				fieldNewEventStart,
				buttonGenerateEvent
			};
		return result;
	}
	
	private MenuItem[] getAllMenuItemDisablelize() {
		MenuItem[] result = {
				menuItemExport
		};
		return result;
	}
	
	private void setDisableAll(boolean b) {
		for (Control c : getAllControls()) {
			c.setDisable(b);
		}
		for (MenuItem m : getAllMenuItemDisablelize()) {
			m.setDisable(b);
		}
	}
	
	//イベント関数
	@FXML
	private void generateNewEvent() {
		String newEventName = fieldNewEventName.getText();
		int newEventStart;
		if (fieldNewEventStart.getText().equals("")) { //$NON-NLS-1$
			newEventStart = 0;
		} else {
			newEventStart = Integer.valueOf(fieldNewEventStart.getText());
			
		}
		initializeNewEventField();
		eventList.addAll(
				new Event(eventList.size(), "無音", 0, false), //$NON-NLS-1$
				new Event(eventList.size() + 1, newEventName, newEventStart, false));
	}
	
	@FXML
	private void onMenuItemExportAction() {
		Properties p = new Properties();
		p.setProperty("eventCount", String.valueOf(eventList.size())); //$NON-NLS-1$
		for (int i = 0; i < eventList.size(); i++) {
			Event e = eventList.get(i);
			String num = String.valueOf(i);
			String name = e.getName();
			if (name.startsWith("無音")) { //$NON-NLS-1$
				name = "null"; //$NON-NLS-1$
			}
			p.setProperty("event" + num + "Name", name); //$NON-NLS-1$ //$NON-NLS-2$
			p.setProperty("event" + num + "Start", String.valueOf(e.getStart())); //$NON-NLS-1$ //$NON-NLS-2$
		}
		try {
			p.store(new FileOutputStream(file), null);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			Alert a = new Alert(AlertType.ERROR);
			a.setHeaderText(Messages.getString("OnkyoPlannerController.ExportFailureHeaderText")); //$NON-NLS-1$
			a.setContentText(Messages.getString("OnkyoPlannerController.ExportFailureContentText1") //$NON-NLS-1$
					+ Messages.getString("OnkyoPlannerController.ExportFailureContentText2") //$NON-NLS-1$
					+ Messages.getString("OnkyoPlannerController.ExportFailureContentText3") + file.getName()); //$NON-NLS-1$
			a.show();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		Alert a = new Alert(AlertType.INFORMATION);
		a.setHeaderText(Messages.getString("OnkyoPlannerController.ExportSuccessHeaderText")); //$NON-NLS-1$
		a.setContentText(Messages.getString("OnkyoPlannerController.ExportSuccessContentText1") //$NON-NLS-1$
				+ Messages.getString("OnkyoPlannerController.ExportSuccessContentText2") + file.getName()); //$NON-NLS-1$
		a.show();
		
		
	}
	
	private void onMenuItemNewAction() {
		File f = new File(Messages.getString("OnkyoPlannerController.DefaultNewFileName")); //$NON-NLS-1$
		
		try {
			if (!f.createNewFile()) {
				Alert a = new Alert(AlertType.WARNING);
				a.setHeaderText(Messages.getString("OnkyoPlannerController.FileGenerateFailureHeaderText")); //$NON-NLS-1$
				a.setContentText(Messages.getString("OnkyoPlannerController.FileGenerateFailureContentText1") //$NON-NLS-1$
						+ Messages.getString("OnkyoPlannerController.FileGenerateFailureContentText2")); //$NON-NLS-1$
				a.show();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Properties p = new Properties();
		p.setProperty("eventCount", "0"); //$NON-NLS-1$ //$NON-NLS-2$
		try {
			p.store(new FileOutputStream(f), null);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	
	private void onMenuItemUpdateAction() {
		menuOpen.getItems().clear();
		String[] plans = OnkyoHelperCollections.findAllPlans();
		for (String s : plans) {
			MenuItem item = new MenuItem(s);
			item.setOnAction((ActionEvent e) -> {
				loadPlan(s);
			});
			menuOpen.getItems().add(item);
		}
		menuOpen.getItems().add(menuItemNew);
		menuOpen.getItems().add(menuItemUpdate);
	}

	
}
