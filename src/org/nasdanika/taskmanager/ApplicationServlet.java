package org.nasdanika.taskmanager;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.nasdanika.html.ApplicationPanel;
import org.nasdanika.html.Bootstrap.Glyphicon;
import org.nasdanika.html.Bootstrap.Style;
import org.nasdanika.html.Button;
import org.nasdanika.html.Dropdown;
import org.nasdanika.html.FontAwesome.Directional;
import org.nasdanika.html.FontAwesome.Spinner;
import org.nasdanika.html.Form;
import org.nasdanika.html.HTMLFactory;
import org.nasdanika.html.Modal;
import org.nasdanika.html.RowContainer.Row;
import org.nasdanika.html.Select;
import org.nasdanika.html.Table;
import org.nasdanika.html.Tag;
import org.nasdanika.html.Tag.TagName;
import org.nasdanika.html.TextArea;
import org.nasdanika.html.Theme;
import org.nasdanika.html.impl.DefaultHTMLFactory;

/**
 * Generates application page.
 */
@WebServlet("/index.html")
public class ApplicationServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	private HTMLFactory htmlFactory = new DefaultHTMLFactory();
       
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Pure API construction of the page. Another way is to use templates and interpolation.
		Tag head = htmlFactory.tag(
				TagName.head,
				htmlFactory.tag(TagName.title, "Task Manager"),
				htmlFactory.tag(TagName.link).attribute("rel", "stylesheet").attribute("href", "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css"),
				htmlFactory.tag(TagName.link).attribute("rel", "stylesheet").attribute("href", "https://maxcdn.bootstrapcdn.com/font-awesome/4.4.0/css/font-awesome.min.css"));

		// Theme is communicated through the 'theme' request parameter
		String theme = request.getParameter("theme");
		if (theme==null) {
			head.content(htmlFactory.tag(TagName.link).attribute("rel", "stylesheet").attribute("href", "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap-theme.min.css"));			
		} else {
			head.content(htmlFactory.tag(TagName.link).attribute("rel", "stylesheet").attribute("href", "https://maxcdn.bootstrapcdn.com/bootswatch/3.3.5/"+theme+"/bootstrap.min.css"));			
		}
		
		head.content(
				htmlFactory.tag(TagName.script).attribute("src", "//code.jquery.com/jquery-1.11.3.min.js"),
				htmlFactory.tag(TagName.script).attribute("src", "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js"),
				htmlFactory.tag(TagName.script).attribute("src", "https://ajax.googleapis.com/ajax/libs/angularjs/1.4.8/angular.min.js"));		
		
		// Application panel with header, body, and footer
		ApplicationPanel appPanel = htmlFactory.applicationPanel()
				.style(Style.PRIMARY)
				.style("margin-top", "1em");
				
		// Theme drop-down on the right
		Tag dropDownToggle = htmlFactory.fontAwesome().directional(Directional.caret_down).getTarget()
				.bootstrap().text().color(Style.DEFAULT) // To make it visible on the PRIMARY background
				.style("cursor", "pointer");
		
		Dropdown<?> themeDropDown = htmlFactory.dropdown(dropDownToggle).style().margin().left("5px");				
		
		themeDropDown.item(htmlFactory.link("index.html", "Bootstrap"));
		for (Theme th: Theme.values()) {
			if (th == Theme.Default || th == Theme.None) {
				continue;
			}
			themeDropDown.item(htmlFactory.link("index.html?theme="+th.name().toLowerCase(), th.name()));
		}
				
		appPanel.header(
				htmlFactory.span("Task Manager"), 
				htmlFactory.div(
						"Theme: ",
						theme==null ? "Bootstrap" : StringUtils.capitalize(theme),
						themeDropDown		
						).bootstrap().pullRight());
		
		// Overlay to block user access to the UI while server operation is in progress.
		Tag overlay = htmlFactory.spinnerOverlay(Spinner.spinner)
				.angular().style("overlayStyle()") // Sizes the overlay 
				.angular().show("inProgress");  // Shows only when HTTP operation in progress
		
		// Table listing tasks
		Table taskTable = htmlFactory.table().striped().bordered();
		
		Row headerRow = taskTable.header().row().style(Style.INFO);
		headerRow.header("Description").bootstrap().text().center();
		headerRow.header("Status").bootstrap().text().center();
		
		Row taskRow = taskTable.body().row().angular().repeat("task in tasks");
		
		Tag descriptionSpan = htmlFactory.span().angular().bind("task.description");
		
		Button editButton = htmlFactory.button(htmlFactory.glyphicon(Glyphicon.pencil))
				.bootstrap().pullRight()
				.style().margin().right("3px")
				.style().border("solid grey 1px")
				.angular().click("editTask($index, task)");

		Button deleteButton = htmlFactory.button(htmlFactory.glyphicon(Glyphicon.trash))
				.bootstrap().pullRight()
				.style("border", "solid grey 1px")
				.angular().click("deleteTask($index, task)");
		
		taskRow.cell(deleteButton, editButton, descriptionSpan);
		
		// Status drop-down allows to change task status without opening the edit dialog
		Dropdown<?> statusDropDown = htmlFactory.caretDropdown();
		statusDropDown.item(htmlFactory.link("#", "Pending").angular().click("setStatus($index, task, 'Pending')"));
		statusDropDown.item(htmlFactory.link("#", "In progress").angular().click("setStatus($index, task, 'In progrress')"));
		statusDropDown.item(htmlFactory.link("#", "Completed").angular().click("setStatus($index, task, 'Completed')"));
		
		taskRow.cell(
				htmlFactory.span().angular().bind("task.status"),
				statusDropDown)
			.bootstrap().text().center()
			.style("white-space", "nowrap");
		
		// Opens "Create task" dialog
		Button newTaskButton = htmlFactory.button("New Task")
				.style(Style.INFO)
				.angular().click("newTask()");
		
		// Modal dialog and a form for creating and editing tasks.
		Modal taskModal = htmlFactory.modal().id("task-modal");
		taskModal.title("{{modal.mode}} task");
		
		Form taskForm = htmlFactory.form();
		
		TextArea descriptionTextArea = htmlFactory.textArea().placeholder("Description").rows(4).angular().model("modal.description");
		taskForm.formGroup("Description", descriptionTextArea, null);
		
		Select statusSelect = htmlFactory.select().angular().model("modal.status");
		statusSelect.option("Pending", "Pending", false, false);
		statusSelect.option("In progress", "In progress", false, false);
		statusSelect.option("Completed", "Completed", false, false);
		
		taskForm.formGroup("Status", statusSelect, null);
		
		taskModal.body(taskForm);
		
		taskModal.footer(
				htmlFactory.button("Submit").angular().click("submitTask()"),
				htmlFactory.button("Cancel").attribute("data-dismiss", "modal"));
		
		appPanel.contentPanel(overlay, taskTable, newTaskButton, taskModal).style("min-height", "500px").id("content-panel");
		
		appPanel.footer(htmlFactory.link("https://github.com/Nasdanika/server/wiki/html", "Documentation"));		
		
		Tag body = htmlFactory.tag(
				TagName.body, 				
				appPanel,
				htmlFactory.tag(TagName.script, getClass().getResource("TaskManager.js")))
				.angular().app("taskManagerApplication")
				.angular().controller("taskManagerController")
				.angular().cloak()
				.style("max-width", "800px")
				.bootstrap().grid().container(); // To center the app panel.
		
		try(Tag routerApp = htmlFactory.tag(TagName.html, head, body)) {
			response.getWriter().write(routerApp.toString());;
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

}
