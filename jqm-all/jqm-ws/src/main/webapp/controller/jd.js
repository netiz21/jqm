'use strict';

var jqmControllers = angular.module('jqmControllers');

jqmControllers.controller('µJdListCtrl', function($scope, $http, $uibModal, µJdDto, µQueueDto)
{
    $scope.jds = null;
    $scope.selected = [];
    $scope.queues =

    $scope.newitem = function()
    {
        var t = new µJdDto({
            description : 'what the job does',
            queueId : 1,
            javaClassName : 'com.company.product.ClassName',
            canBeRestarted : true,
            highlander : false,
            jarPath : 'relativepath/to/file.jar',
            enabled : true,
            parameters : [],
        });
        $scope.jds.push(t);
        $scope.selected.push(t);
    };

    $scope.save = function()
    {
        // Save and refresh the table - ID may have been generated by the server.
        µJdDto.saveAll({}, $scope.jds, $scope.refresh);
    };

    $scope.refresh = function()
    {
        $scope.selected.length = 0;
        $scope.jds = µJdDto.query();
        $scope.queues = µQueueDto.query();
    };

    // Only remove from list - save() will sync the list with the server so no need to delete it from server now
    $scope.remove = function()
    {
        var q = null;
        for ( var i = 0; i < $scope.selected.length; i++)
        {
            q = $scope.selected[i];
            $scope.jds.splice($scope.jds.indexOf(q), 1);
        }
        $scope.selected.length = 0;
    };
    
    $scope.filterOptions = {
            filterText : '',
        };

    $scope.gridOptions = {
        data : 'jds',
        enableCellSelection : true,
        enableRowSelection : true,
        enableCellEditOnFocus : true,
        enableColumnResize : true,
        enableColumnReordering : false,
        multiSelect : true,
        showSelectionCheckbox : true,
        selectWithCheckboxOnly : true,
        selectedItems : $scope.selected,
        virtualizationThreshold: 10,
        plugins : [ new ngGridFlexibleHeightPlugin() ],
        filterOptions : $scope.filterOptions,
        columnDefs : [
                {
                    field : 'applicationName',
                    displayName : 'Name',
                    width : '**',
                },
                {
                    field : 'description',
                    displayName : 'Description',
                    width : '***',
                },
                {
                    field : 'javaClassName',
                    displayName : 'Class to launch',
                    width : '**',
                },
                {
                    field : 'jarPath',
                    displayName : 'Path to the jar (relative to repo)',
                    width : '***',
                },
                {
                    field : 'canBeRestarted',
                    displayName : 'R',
                    cellTemplate : '<div class="ngSelectionCell" ng-class="col.colIndex()"><span class="glyphicon {{ row.entity[col.field] ? \'glyphicon-ok\' : \'glyphicon-remove\' }}"></span></div>',
                    editableCellTemplate : '<div class="ngSelectionCell" ng-class="col.colIndex()"><input type="checkbox" ng-input="COL_FIELD" ng-model="COL_FIELD"/></div>',
                    width : 25,
                },
                {
                    field : 'highlander',
                    displayName : 'H',
                    cellTemplate : '<div class="ngSelectionCell" ng-class="col.colIndex()"><span class="glyphicon {{ row.entity[col.field] ? \'glyphicon-ok\' : \'glyphicon-remove\' }}"></span></div>',
                    editableCellTemplate : '<div class="ngSelectionCell" ng-class="col.colIndex()"><input type="checkbox" ng-input="COL_FIELD" ng-model="COL_FIELD"/></div>',
                    width : 25,
                },
                {
                    field : 'queueId',
                    displayName : 'Queue',
                    cellTemplate : '<div class="ngCellText" ng-class="col.colIndex()">'
                            + '<span ng-cell-text>{{ (row.getProperty("queueId") | getByProperty:"id":queues).name }}</span></div>',
                    editableCellTemplate : '<select ng-cell-input ng-input="COL_FIELD" ng-model="COL_FIELD" '
                            + 'ng-options="q.id as q.name for q in queues"></select>'

                },
                {
                    field : 'application',
                    displayName : 'Application'
                },
                {
                    field : 'module',
                    displayName : 'Module'
                },
                {
                    field : 'keyword1',
                    displayName : 'Keyword1'
                },
                {
                    field : 'keyword2',
                    displayName : 'Keyword2'
                },
                {
                    field : 'keyword3',
                    displayName : 'Keyword3'
                },
                {
                	field: 'reasonableRuntimeLimitMinute',
                	displayName: 'AlertMn',
                	editableCellTemplate : '<input type="number" ng-class="\'colt\' + col.index" ng-input="COL_FIELD" ng-model="COL_FIELD" min="1" max="1440" />'
                },
                {
                    field : 'enabled',
                    displayName : 'E',
                    cellTemplate : '<div class="ngSelectionCell" ng-class="col.colIndex()"><span class="glyphicon {{ row.entity[col.field] ? \'glyphicon-ok\' : \'glyphicon-remove\' }}"></span></div>',
                    editableCellTemplate : '<div class="ngSelectionCell" ng-class="col.colIndex()"><input type="checkbox" ng-input="COL_FIELD" ng-model="COL_FIELD"/></div>',
                    width : 25,
                },
                ]
    };

    $scope.prms = function()
    {
    	$uibModal.open({
            templateUrl : './template/jd_prms.html',
            controller : 'jdPrms',
            size : 'lg',
            resolve : {
                jd : function()
                {
                    return $scope.selected[0];
                }
            },
        });

    };
    $scope.refresh();
});

jqmApp.controller('jdPrms', function($scope, $uibModalInstance, jd)
{
    $scope.selectedJd = jd;
    $scope.data = {
        newKey : null,
        newValue : null
    };

    $scope.addPrm = function()
    {
        var np = {};
        np.key = $scope.data.newKey;
        np.value = $scope.data.newValue;
        $scope.selectedJd.parameters.push(np);
    };

    $scope.delPrm = function(prm)
    {
        $scope.selectedJd.parameters.splice($scope.selectedJd.parameters.indexOf(prm), 1);
    };

    $scope.ok = function()
    {
        $uibModalInstance.close();
    };
});
