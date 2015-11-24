angular.module("taskManagerApplication", []).controller("taskManagerController", function($scope, $http) {
	
	// Sizes overlay 
	$scope.overlayStyle = function() {
		
		return {
			width: $("#content-panel").width()+"px",
			height: $("#content-panel").height()+"px"
		}
		
	};
	
	// Set to true before initiating server interaction and to false when response arrives.
	// Overlay is displayed when inProgress is set to true.
	$scope.inProgress = true;	
	
	// Loads a list of tasks from the server
	$http.get('tasks'). 
    then(function(response) {
        $scope.tasks = response.data;
        $scope.inProgress = false;        
    }, function(response) {
        $scope.inProgress = false;        
		alert("Error loading tasks: "+response.statusText);
    }); 
	
	// Sets task status - used by the status drop-down
	$scope.setStatus = function(index, task, status) {
		$scope.inProgress = true;
		task.status = status;
		$http.put('tasks/'+index, task). 
	    then(function(response) {
	        $scope.tasks = response.data;
	        $scope.inProgress = false;        
	    }, function(response) {
	        $scope.inProgress = false;        
			alert("Error updating task: "+response.statusText);
	    }); 
	}
	
	// Deletes task
	$scope.deleteTask = function(index, task) {
		if (confirm("Delete task '"+task.description+"'?")) {
			$scope.inProgress = true;
			task.status = status;
			$http.delete('tasks/'+index). 
		    then(function(response) {
		        $scope.tasks = response.data;
		        $scope.inProgress = false;        
		    }, function(response) {
		        $scope.inProgress = false;        
				alert("Error updating task: "+response.statusText);
		    });
		}
	}	
	
	// Modal form is bound to this model.
	$scope.modal = {};
	
	// Sets initial values in the modal model and shows the create/edit task dialog
	$scope.newTask = function() {
		$scope.modal.mode = "Create";
		$scope.modal.description = "";
		$scope.modal.status = "Pending";
		jQuery("#task-modal").modal('show');
	};
	
	// Sets values in the modal model from the tasks to be edited and shows the create/edit task dialog
	$scope.editTask = function(index, task) {
		$scope.modal.mode = "Edit";
		$scope.modal.index = index;
		$scope.modal.description = task.description;
		$scope.modal.status = task.status;
		jQuery("#task-modal").modal('show');
	};		

	// Submits task data to the server - post for new tasks, put for existing.
	// Invoked by the modal dialog submit button
	$scope.submitTask = function() {
		jQuery("#task-modal").modal('hide');
		$scope.inProgress = true;
		if ($scope.modal.mode == "Create") {
			$http.post('tasks', {
				description : $scope.modal.description,
				status : $scope.modal.status
			}). 
		    then(function(response) {
		        $scope.tasks = response.data;
		        $scope.inProgress = false;        
		    }, function(response) {
		        $scope.inProgress = false;        
				alert("Error creating task: "+response.statusText);
		    }); 			
		} else {
			$http.put('tasks/'+$scope.modal.index, {
				description : $scope.modal.description,
				status : $scope.modal.status
			}). 
		    then(function(response) {
		        $scope.tasks = response.data;
		        $scope.inProgress = false;        
		    }, function(response) {
		        $scope.inProgress = false;        
				alert("Error updating task: "+response.statusText);
		    }); 			
		}
	}
	                
});
