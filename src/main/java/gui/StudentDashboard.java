package gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import main.AppSchedulerGUI;
import scheduling.TimeSlot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StudentDashboard {
	private AppSchedulerGUI app;
	private TabPane tabPane;

	// Live-observable list for the Manage tab (confirmed + waitlisted slots)
	private ObservableList<TimeSlot> myBookedSlots = FXCollections.observableArrayList();

	public StudentDashboard(AppSchedulerGUI app) {
		this.app = app;
		// Pre-populate: include both confirmed and waitlisted slots
		for (TimeSlot slot : app.systemTimeSlots) {
			boolean confirmed = slot.getConfirmedStudents() != null
					&& slot.getConfirmedStudents().contains(app.currentStudent);
			boolean waitlisted = slot.getWaitlist() != null
					&& slot.getWaitlist().contains(app.currentStudent);
			if (confirmed || waitlisted)
				myBookedSlots.add(slot);
		}
	}

	public VBox getContent() {
		VBox layout = new VBox(0);
		layout.setStyle("-fx-background-color: " + AppStyles.BG_PAGE + ";");

		// --- Header bar matching LoginScreen sidebar style ---
		HBox header = new HBox();
		header.setPadding(new Insets(16, 28, 16, 28));
		header.setAlignment(Pos.CENTER_LEFT);
		header.setStyle("-fx-background-color: #0C447C;");

		Label appName = new Label("TimeTable");
		appName.setStyle(
				"-fx-font-family: 'Georgia'; -fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");
		Label divider = new Label("  |  ");
		divider.setStyle("-fx-text-fill: #85B7EB; -fx-font-size: 18px;");
		Label title = new Label("Student Hub - " + app.currentStudent.getName());
		title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #D1E8FF;");

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		Button logoutBtn = new Button("Log Out");
		logoutBtn.setStyle(
				"-fx-background-color: transparent; -fx-border-color: #85B7EB; -fx-border-radius: 6; " +
						"-fx-text-fill: white; -fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 6 14;");
		logoutBtn.setOnAction(e -> {
			if (app.confirmAction("Confirm Log Out", "Are you sure you want to log out?")) {
				app.showLogin();
			}
		});
		header.getChildren().addAll(appName, divider, title, spacer, logoutBtn);

		// Tabs
		tabPane = new TabPane();
		tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
		tabPane.setStyle("-fx-background-color: " + AppStyles.BG_PAGE + ";");

		Tab bookTab = new Tab("Book Appointments");
		bookTab.setContent(createBookingTab());

		Tab notifTab = new Tab("Notifications");
		notifTab.setContent(createNotificationsTab());

		Tab manageTab = new Tab("Manage Schedules");
		manageTab.setContent(createManageTab());

		tabPane.getTabs().addAll(bookTab, notifTab, manageTab);
		VBox.setVgrow(tabPane, Priority.ALWAYS);

		layout.getChildren().addAll(header, tabPane);
		return layout;
	}

	// Tab 1: Booking (card layout matching Faculty's My Timeslots)
	private VBox createBookingTab() {
		VBox box = new VBox(15);
		box.setPadding(new Insets(20));
		box.setStyle("-fx-background-color: " + AppStyles.BG_PAGE + ";");

		Label sectionHeader = new Label("Available Appointment Slots");
		sectionHeader.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
		Label subHeader = new Label("Select a slot and click 'Book' to reserve your spot.");
		subHeader.setStyle("-fx-font-size: 12px; -fx-text-fill: " + AppStyles.TEXT_MUTED + ";");

		Label statusLbl = new Label();
		statusLbl.setWrapText(true);

		ObservableList<HBox> cardItems = FXCollections.observableArrayList();
		final TimeSlot[] selectedSlot = { null };
		final HBox[] selectedCard = { null };

		refreshBookingCards(cardItems, selectedSlot, selectedCard, statusLbl);

		ListView<HBox> slotListView = new ListView<>(cardItems);
		slotListView.setPlaceholder(new Label("No approved slots available yet."));
		slotListView.setStyle("-fx-background-color: " + AppStyles.BG_PAGE + ";");
		VBox.setVgrow(slotListView, Priority.ALWAYS);
		slotListView.setSelectionModel(new NoSelectionModel<HBox>());

		Button bookBtn = AppStyles.primaryButton("Book Selected Slot");
		bookBtn.setPrefHeight(40);
		bookBtn.setOnAction(e -> {
			if (selectedSlot[0] == null) {
				AppStyles.showStatus(statusLbl, "Please select a slot first.", AppStyles.STATUS_DECLINED, "#FDE8E8");
				return;
			}
			TimeSlot slot = selectedSlot[0];

			// Prevent duplicate booking (confirmed OR waitlist)
			boolean alreadyConfirmed = slot.getConfirmedStudents().contains(app.currentStudent);
			boolean alreadyWaitlisted = slot.getWaitlist().contains(app.currentStudent);
			if (alreadyConfirmed) {
				AppStyles.showStatus(statusLbl, "You have already booked this slot.", AppStyles.TEXT_MUTED,
						AppStyles.BG_FIELD);
				return;
			}
			if (alreadyWaitlisted) {
				AppStyles.showStatus(statusLbl, "You are already on the waitlist for this slot.",
						AppStyles.STATUS_PENDING, AppStyles.WARN_BG);
				return;
			}

			boolean isFull = slot.getConfirmedStudents().size() >= slot.getSlotCapacity();
			slot.bookSlot(app.currentStudent);
			app.addNotification(slot.getCreatorId(),
					"📌 Student " + app.currentStudent.getName() + " booked your slot: \""
							+ slot.getTitle() + "\" on " + slot.getLocalDate()
							+ " (" + slot.getStartTime() + " – " + slot.getEndTime() + ").");
			main.DataManager.saveState(app.systemTimeSlots);

			if (isFull) {
				AppStyles.showStatus(statusLbl,
						"⏳ Slot is full - you have been added to the waitlist.",
						AppStyles.STATUS_PENDING, AppStyles.WARN_BG);
				// Notify the student themselves that they are waitlisted
				app.addNotification(app.currentStudent.getUserId(),
						"⏳ You are on the waitlist for \"" + slot.getTitle() + "\" on " + slot.getLocalDate()
								+ " (" + slot.getStartTime() + " – " + slot.getEndTime() + "). "
								+ "You will be confirmed automatically if a spot opens up.");
			} else {
				AppStyles.showStatus(statusLbl,
						"✅ Booked: " + slot.getTitle() + " on " + slot.getLocalDate(),
						AppStyles.STATUS_APPROVED, "#D1FAE5");
				if (!myBookedSlots.contains(slot))
					myBookedSlots.add(slot);
			}

			// Rebuild cards so spot counts update live
			selectedSlot[0] = null;
			selectedCard[0] = null;
			refreshBookingCards(cardItems, selectedSlot, selectedCard, statusLbl);
		});

		HBox btnRow = new HBox(bookBtn);
		btnRow.setAlignment(Pos.CENTER_LEFT);

		box.getChildren().addAll(sectionHeader, subHeader, slotListView, statusLbl, btnRow);
		return box;
	}

	/**
	 * Rebuilds the card list from current system data - call after any booking to
	 * reflect live counts.
	 */
	private void refreshBookingCards(ObservableList<HBox> cardItems, TimeSlot[] selectedSlot,
			HBox[] selectedCard, Label statusLbl) {
		cardItems.clear();
		List<TimeSlot> approvedSlots = new ArrayList<>();
		for (TimeSlot s : app.systemTimeSlots) {
			if ("APPROVED".equals(s.getStatus()))
				approvedSlots.add(s);
		}
		approvedSlots.sort((a, b) -> {
			int d = a.getLocalDate().compareTo(b.getLocalDate());
			return d != 0 ? d : a.getStartTime().compareTo(b.getStartTime());
		});
		for (TimeSlot slot : approvedSlots) {
			cardItems.add(buildSlotCard(slot, selectedSlot, selectedCard, cardItems, statusLbl));
		}
	}

	private HBox buildSlotCard(TimeSlot slot, TimeSlot[] selectedSlot, HBox[] selectedCard,
			ObservableList<HBox> cardItems, Label statusLbl) {
		HBox card = new HBox(15);
		card.setAlignment(Pos.CENTER_LEFT);
		card.setPadding(new Insets(12, 16, 12, 16));
		card.setStyle(cardBaseStyle());

		int confirmed = slot.getConfirmedStudents().size();
		int capacity = slot.getSlotCapacity();
		int spotsLeft = capacity - confirmed;
		boolean isFull = spotsLeft <= 0;
		boolean alreadyBooked = slot.getConfirmedStudents().contains(app.currentStudent)
				|| slot.getWaitlist().contains(app.currentStudent);

		// Left status stripe - amber for full (still registerable), red only for
		// already-waitlisted
		String stripeColor;
		if (alreadyBooked)
			stripeColor = isFull ? AppStyles.STATUS_PENDING : AppStyles.STATUS_APPROVED;
		else if (isFull)
			stripeColor = AppStyles.STATUS_PENDING;
		else if (spotsLeft == 1)
			stripeColor = AppStyles.STATUS_PENDING;
		else
			stripeColor = AppStyles.STATUS_APPROVED;

		Label stripe = new Label();
		stripe.setMinWidth(5);
		stripe.setPrefWidth(5);
		stripe.setPrefHeight(56);
		stripe.setStyle("-fx-background-color: " + stripeColor + "; -fx-background-radius: 3;");

		// Card content - user-requested layout
		VBox info = new VBox(4);

		// Line 1: Meeting name @ Faculty name
		Label nameLbl = new Label(slot.getTitle() + " @ " + slot.getCreatorName());
		nameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1A1A2E;");

		// Line 2: Date StartTime–EndTime • Room
		Label timeLbl = new Label(slot.getLocalDate() + "   " + slot.getStartTime()
				+ " – " + slot.getEndTime() + "   •   " + slot.getResource().getName());
		timeLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: " + AppStyles.TEXT_MUTED + ";");

		// Line 3: Slots left - full slots invite waitlist registration, never block it
		String slotsText;
		if (alreadyBooked && isFull)
			slotsText = "⏳ You are on the waitlist";
		else if (alreadyBooked)
			slotsText = "✅ You are booked   |   " + spotsLeft + " spot" + (spotsLeft != 1 ? "s" : "") + " left";
		else if (isFull)
			slotsText = "📋 Full - you can still join waitlist";
		else
			slotsText = spotsLeft + " spot" + (spotsLeft != 1 ? "s" : "") + " left  /  " + capacity + " total";

		Label slotsLbl = new Label(slotsText);
		String slotsColor = alreadyBooked
				? (isFull ? AppStyles.STATUS_PENDING : AppStyles.STATUS_APPROVED)
				: (isFull ? AppStyles.STATUS_PENDING : "#64748B");
		slotsLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: " + slotsColor + "; -fx-font-weight: bold;");

		info.getChildren().addAll(nameLbl, timeLbl, slotsLbl);
		card.getChildren().addAll(stripe, info);

		// Already-booked cards are dimmed and non-clickable; everything else is
		// selectable
		if (alreadyBooked) {
			card.setStyle(cardBaseStyle() + " -fx-opacity: 0.65;");
		} else {
			// Full slots are still clickable - student will be placed on waitlist
			card.setOnMouseClicked(e -> {
				if (selectedCard[0] != null)
					selectedCard[0].setStyle(cardBaseStyle());
				selectedSlot[0] = slot;
				selectedCard[0] = card;
				card.setStyle(cardSelectedStyle());
			});
		}

		return card;
	}

	private String cardBaseStyle() {
		return "-fx-background-color: " + AppStyles.WHITE + "; -fx-border-color: #E2E8F0; "
				+ "-fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;";
	}

	private String cardSelectedStyle() {
		return "-fx-background-color: #EFF6FF; -fx-border-color: " + AppStyles.BLUE_MID + "; "
				+ "-fx-border-radius: 8; -fx-background-radius: 8; -fx-border-width: 2; -fx-cursor: hand;";
	}

	// Tab 2: Notifications
	private VBox createNotificationsTab() {
		VBox box = new VBox(12);
		box.setPadding(new Insets(20));
		box.setStyle("-fx-background-color: " + AppStyles.BG_PAGE + ";");

		ObservableList<VBox> items = FXCollections.observableArrayList();

		List<scheduling.Notification> myNotifs = new ArrayList<>();
		for (scheduling.Notification n : app.systemNotifications) {
			if (n.getUserId().equals(app.currentStudent.getUserId()))
				myNotifs.add(n);
		}
		Collections.reverse(myNotifs); // Newest first

		for (scheduling.Notification n : myNotifs)
			items.add(buildNotifCard(n));

		ListView<VBox> notifList = new ListView<>(items);
		notifList.setPlaceholder(new Label("No notifications yet."));
		notifList.setStyle("-fx-background-color: " + AppStyles.BG_PAGE + ";");
		VBox.setVgrow(notifList, Priority.ALWAYS);

		Label titleLbl = new Label("My Notifications");
		titleLbl.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
		Label countLbl = new Label(myNotifs.size() + " message(s)");
		countLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: " + AppStyles.TEXT_MUTED + ";");

		HBox topRow = new HBox(10, titleLbl, new Region(), countLbl);
		HBox.setHgrow(topRow.getChildren().get(1), Priority.ALWAYS);
		topRow.setAlignment(Pos.CENTER_LEFT);

		Button clearBtn = AppStyles.dangerButton("Clear All Notifications");
		clearBtn.setPrefHeight(36);
		clearBtn.setOnAction(e -> {
			if (app.confirmAction("Clear Notifications", "Remove all your notifications?")) {
				app.clearNotifications(app.currentStudent.getUserId());
				items.clear();
			}
		});

		box.getChildren().addAll(topRow, notifList, clearBtn);
		return box;
	}

	private VBox buildNotifCard(scheduling.Notification n) {
		VBox card = new VBox(4);
		card.setPadding(new Insets(10, 14, 10, 14));
		card.setStyle("-fx-background-color: " + AppStyles.WHITE
				+ "; -fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-background-radius: 8;");
		Label timeLbl = new Label(n.getFormattedTime());
		timeLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #94A3B8;");
		Label msgLbl = new Label(n.getMessage());
		msgLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #334155;");
		msgLbl.setWrapText(true);
		card.getChildren().addAll(timeLbl, msgLbl);
		return card;
	}

	// Tab 3: Manage Bookings — shows confirmed AND waitlisted slots
	private VBox createManageTab() {
		VBox box = new VBox(15);
		box.setPadding(new Insets(20));
		box.setStyle("-fx-background-color: " + AppStyles.BG_PAGE + ";");

		Label sectionHeader = new Label("My Appointments & Waitlist");
		sectionHeader.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
		Label subHeader = new Label(
				"Green stripe = confirmed. Amber stripe = on waitlist (auto-promoted when a spot opens).");
		subHeader.setStyle("-fx-font-size: 12px; -fx-text-fill: " + AppStyles.TEXT_MUTED + ";");

		ListView<TimeSlot> bookingList = new ListView<>(myBookedSlots);
		bookingList.setPlaceholder(new Label("You have no bookings or waitlisted slots."));
		bookingList.setStyle("-fx-background-color: " + AppStyles.BG_PAGE + ";");
		bookingList.setCellFactory(param -> new ListCell<TimeSlot>() {
			@Override
			protected void updateItem(TimeSlot slot, boolean empty) {
				super.updateItem(slot, empty);
				if (empty || slot == null) { setText(null); setGraphic(null); return; }

				boolean isConfirmed = slot.getConfirmedStudents().contains(app.currentStudent);
				boolean isWaitlisted = slot.getWaitlist().contains(app.currentStudent);

				// Find queue position if waitlisted
				int waitPos = 0;
				if (isWaitlisted) {
					for (users.Student s : slot.getWaitlist()) {
						waitPos++;
						if (s.equals(app.currentStudent)) break;
					}
				}

				HBox card = new HBox(15);
				card.setAlignment(Pos.CENTER_LEFT);
				card.setPadding(new Insets(12, 16, 12, 16));
				card.setStyle("-fx-background-color: " + AppStyles.WHITE
						+ "; -fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-background-radius: 8;");

				// Green stripe = confirmed, Amber = waitlisted
				Label stripe = new Label();
				stripe.setMinWidth(5); stripe.setPrefWidth(5); stripe.setPrefHeight(60);
				stripe.setStyle("-fx-background-color: "
						+ (isConfirmed ? AppStyles.STATUS_APPROVED : AppStyles.STATUS_PENDING)
						+ "; -fx-background-radius: 3;");

				VBox info = new VBox(4);

				// Line 1: Meeting @ Faculty
				Label nameLbl = new Label(slot.getTitle() + " @ " + slot.getCreatorName());
				nameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1A1A2E;");

				// Line 2: Date time - Room
				Label timeLbl = new Label(slot.getLocalDate() + "   "
						+ slot.getStartTime() + " - " + slot.getEndTime()
						+ "   |   " + slot.getResource().getName());
				timeLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: " + AppStyles.TEXT_MUTED + ";");

				// Line 3: Status
				String statusText;
				String statusColor;
				if (isConfirmed) {
					int pos = slot.getConfirmedStudents().indexOf(app.currentStudent) + 1;
					statusText = "Confirmed   |   Seat #" + pos + " of " + slot.getSlotCapacity();
					statusColor = AppStyles.STATUS_APPROVED;
				} else if (isWaitlisted) {
					statusText = "On waitlist   |   Queue position: #" + waitPos;
					statusColor = AppStyles.STATUS_PENDING;
				} else {
					statusText = "(Status unknown)";
					statusColor = AppStyles.TEXT_MUTED;
				}
				Label statusLbl2 = new Label(statusText);
				statusLbl2.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + statusColor + ";");

				info.getChildren().addAll(nameLbl, timeLbl, statusLbl2);
				card.getChildren().addAll(stripe, info);
				setGraphic(card);
			}
		});
		VBox.setVgrow(bookingList, Priority.ALWAYS);

		Button goBookBtn = AppStyles.ghostButton("Browse More Slots");
		goBookBtn.setPrefHeight(38);
		goBookBtn.setOnAction(e -> tabPane.getSelectionModel().select(0));

		Button cancelBtn = AppStyles.dangerButton("Cancel / Leave Waitlist");
		cancelBtn.setPrefHeight(38);
		cancelBtn.setOnAction(e -> {
			TimeSlot selected = bookingList.getSelectionModel().getSelectedItem();
			if (selected == null) {
				app.showAlert(javafx.scene.control.Alert.AlertType.WARNING,
						"No selection", "Please select a slot to cancel.");
				return;
			}
			boolean onWaitlist = selected.getWaitlist().contains(app.currentStudent);
			String confirmMsg = onWaitlist
					? "Leave the waitlist for \"" + selected.getTitle() + "\"?"
					: "Cancel your confirmed booking for \"" + selected.getTitle() + "\"?";
			if (app.confirmAction("Confirm Cancellation", confirmMsg)) {
				// cancelBooking returns the promoted student (if any)
				users.Student promoted = selected.cancelBooking(app.currentStudent);
				main.DataManager.saveState(app.systemTimeSlots);
				myBookedSlots.remove(selected);

				// Notify the promoted waitlisted student
				if (promoted != null) {
					app.addNotification(promoted.getUserId(),
							"A spot opened up! You have been moved from the waitlist to confirmed for \""
							+ selected.getTitle() + "\" on " + selected.getLocalDate()
							+ " (" + selected.getStartTime() + " - " + selected.getEndTime() + ")."
							+ " Your booking is now confirmed.");
				}
			}
		});

		HBox btnRow = new HBox(10, goBookBtn, cancelBtn);
		btnRow.setAlignment(Pos.CENTER_LEFT);

		box.getChildren().addAll(sectionHeader, subHeader, bookingList, btnRow);
		return box;
	}

	// Suppress default ListView selection highlight - cards handle their own style
	private static class NoSelectionModel<T> extends MultipleSelectionModel<T> {
		@Override
		public ObservableList<Integer> getSelectedIndices() {
			return FXCollections.emptyObservableList();
		}

		@Override
		public ObservableList<T> getSelectedItems() {
			return FXCollections.emptyObservableList();
		}

		@Override
		public void selectIndices(int i, int... ints) {
		}

		@Override
		public void selectAll() {
		}

		@Override
		public void selectFirst() {
		}

		@Override
		public void selectLast() {
		}

		@Override
		public void clearAndSelect(int i) {
		}

		@Override
		public void select(int i) {
		}

		@Override
		public void select(T t) {
		}

		@Override
		public void clearSelection(int i) {
		}

		@Override
		public void clearSelection() {
		}

		@Override
		public boolean isSelected(int i) {
			return false;
		}

		@Override
		public boolean isEmpty() {
			return true;
		}

		@Override
		public void selectPrevious() {
		}

		@Override
		public void selectNext() {
		}
	}
}