package org.nasdanika.taskmanager;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Tasks servlet manages in-memory task list stored in a session. Initial task list is loaded from tasks.json resource.
 */
@WebServlet({ "/tasks", "/tasks/*" })
public class TaskServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * Retrieves task list from a session attribute. 
	 * If there is no such attribute, reads 'tasks.json' resource and stores it as a session attribute.
	 * This method sleeps for a second to demonstrate how overlay works on the client side.
	 * @param request
	 * @return
	 * @throws IOException
	 * @throws ServletException
	 */
	private JSONArray getTasks(HttpServletRequest request) throws IOException, ServletException {
		try {
			Thread.sleep(1000); // Artificial delay to demonstrate overlay in action.
			HttpSession session = request.getSession();
			JSONArray tasks = (JSONArray) session.getAttribute("tasks");
			if (tasks == null) {
				try (Reader reader = new InputStreamReader(getClass().getResourceAsStream("tasks.json"))) {
					tasks = new JSONArray(new JSONTokener(reader));
					session.setAttribute("tasks", tasks);
				}
			}
			return tasks;
		} catch (JSONException | InterruptedException e) {
			throw new ServletException(e);
		}			
	}

	/**
	 * Renders tasks list as JSON array
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (request.getPathInfo()==null) {
			response.getWriter().write(getTasks(request).toString());
		} else {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	/**
	 * Creates a new task.
	 * Takes JSON object in request body and adds it to the task list.
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {		
		if (request.getPathInfo()==null) {
			try {
				JSONObject task = new JSONObject(new JSONTokener(request.getReader()));
				JSONArray tasks = getTasks(request);			
				tasks.put(task);
				System.out.println("Created a new task: "+task);
				response.getWriter().write(tasks.toString());
			} catch (JSONException e) {
				throw new ServletException(e);
			}
		} else {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	/**
	 * Updates task in the task list. Task index is taken from the request path, e.g. path /tasks/5 corresponds to 
	 * the task with index 5. Task data is passed in JSON format in request body.
	 * @see HttpServlet#doPut(HttpServletRequest, HttpServletResponse)
	 */
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			JSONObject task = new JSONObject(new JSONTokener(request.getReader()));
			JSONArray tasks = getTasks(request);			
			int idx = Integer.parseInt(request.getPathInfo().substring(1));
			tasks.put(idx, task);
			System.out.println("Updated task at index "+idx+" to "+task);
			response.getWriter().write(tasks.toString());
		} catch (JSONException e) {
			throw new ServletException(e);
		}
	}

	/**
	 * Deletes task from the task list. Task index is taken from the request path, e.g. path /tasks/5 corresponds to
	 * the task with index 5. 
	 * @see HttpServlet#doDelete(HttpServletRequest, HttpServletResponse)
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONArray tasks = getTasks(request);			
		int idx = Integer.parseInt(request.getPathInfo().substring(1));
		tasks.remove(idx);
		System.out.println("Removed task at index "+idx);
		response.getWriter().write(tasks.toString());
	}

}
