var taskStatusModule = angular.module('taskStatusModule', []);

taskStatusModule.controller("TaskStatusCtrl", function ($scope, TaskStatus, TaskStatusFacade, $location) {
    $scope.taskStatus = TaskStatus;
    
    $scope.findAll = function(){
        TaskStatusFacade.findAll()
        .success(function(data){
            $scope.taskStatuses = data;
        }).error(function(data, status){
            console.log("Requests types not found. Error: "+data+" Status: "+status);
        });
    };
    
    $scope.findAll();
    
    $scope.create = function(){
        console.log($scope.taskStatus);
        TaskStatusFacade.create($scope.taskStatus)
        .success(function(data){
            console.log("New task status was saved.");
            $scope.findAll();
        }).error(function(data, status){
            console.log("Error saving task status. Error: "+data+" Status: "+status);
        });
    };
    
     $scope.delete = function (taskStatus) {
        TaskStatusFacade.delete(taskStatus).success(function () {
            var index = $scope.taskStatuses.indexOf(taskStatus);

            if (index > -1) {
                $scope.taskStatuses.splice(index, 1);
            }
        }).error(function (data, status) {

            console.log("Ошибка при удалении статуса задачи: " + taskStatus.name + ". Status: " + status);

        });
    };
});

taskStatusModule.factory('TaskStatus', function () {
    var taskStatus = {
        id: null,
        code: null,
        name: null
    };
    return taskStatus;
});

taskStatusModule.factory('TaskStatusFacade', function ($http) {
    var taskStatusFacade = {};
    
    taskStatusFacade.findAll = function () {
        return $http({
            method: "GET",
            withCredentials: true,
            url: "./webresources/task_status"
        });
    };
    
    taskStatusFacade.create = function (taskStatus) {
        return $http({
            method: "POST",
            data: taskStatus,
            withCredentials: true,
            url: "./webresources/task_status"
        });
    };
    
    taskStatusFacade.delete = function (taskStatus) {
        return $http({
            method: "DELETE",
            withCredentials: true,
            url: "./webresources/task_status/"+taskStatus.id
        });
    };
    
    return taskStatusFacade;
});