var taskModule = angular.module("taskModule", [
    'ui.bootstrap.datetimepicker',
    'auctionModule',
    'requestModule'
]);

taskModule.controller("TaskCtrl", function ($scope, $location, TaskFacade, Task, 
                                  AuctionFacade, RequestTypeFacade) {

    $scope.task = Task;
    AuctionFacade.findAll()
            .success(function (data) {
                $scope.auctions = data;
            }).error(function (data, status) {
        console.log("Ошибка получения аукционов. Status: " + status, '; Data: ' + data);
    });

    RequestTypeFacade.findAll()
            .success(function (data) {
                $scope.requestTypes = data;
            }).error(function (data, status) {
                console.log("Ошибка получения типов запроса. Status: " + status, '; Data: ' + data);
            });

    $scope.create = function () {
        console.log($scope.task);
        TaskFacade.create($scope.task)
                .success(function (data) {
                    $location.path("/");
                }).error(function (data, status) {
            console.log("Ошибка сохранения задачи. Status: " + status, '; Data: ' + data);
        });
    };

    $scope.open = function ($event) {
        $event.preventDefault();
        $event.stopPropagation();

        $scope.opened = true;
    };
});

taskModule.factory("Task", function () {

    var task = {
        id: null,
        url: null,
        bid: null,
        beginDate: null,
        startTime: null,
        endTime: null,
        requestType: {
            id: null,
            name: null
        },
        auctionType: {
            id: null,
            name: null,
            url: null
        }
    };
    return task;
});

taskModule.factory("TaskFacade", function ($http) {
    var taskFacade = {};
    taskFacade.findAll = function () {
        return $http({
            method: "GET",
            withCredentials: true,
            url: "./webresources/task"
        });
    };

    taskFacade.create = function (task) {
        return $http({
            method: "POST",
            withCredentials: true,
            url: "./webresources/task",
            data: task
        });
    };

    return taskFacade;
});