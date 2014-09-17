var mainModule = angular.module("mainModule", ['taskModule']);

mainModule.controller('IndexCtrl', function TasksCtrl($scope, TaskFacade) {


    $scope.findAll = function () {
        TaskFacade.findAll().success(function (data, status, headers, config) {
            $scope.tasks = data;
            $scope.tasks.forEach(function (value) {
                value.beginDate = new Date(value.beginDate);
                value.startTime = new Date(value.startTime);
                value.endTime = new Date(value.endTime);
            });
        }).error(function (data, status, headers, config) {

            console.log("Ошибка при получении задач. Status: " + status, '; Data: ' + data);

        });
    };
    $scope.findAll();

    $scope.delete = function (task) {
        TaskFacade.delete(task).success(function (data) {
            var index = $scope.tasks.indexOf(task);

            if (index > -1) {
                $scope.tasks.splice(index, 1);
            }
        }).error(function (data, status, headers, config) {

            console.log("Ошибка при получении задач. Status: " + status, '; Data: ' + data);

        });
    };

});

