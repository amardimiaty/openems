import { Component, Input, OnInit, OnChanges, ViewChild, AfterViewInit, SimpleChanges } from '@angular/core';
import { Subject } from 'rxjs/Subject';
import { BaseChartDirective } from 'ng2-charts/ng2-charts';
import * as moment from 'moment';

import { Dataset, EMPTY_DATASET, Device, Config, QueryReply, Summary } from './../../../../shared/shared';
import { DEFAULT_TIME_CHART_OPTIONS, CHART_OPTIONS } from './../shared';

@Component({
  selector: 'energychart',
  templateUrl: './energychart.component.html'
})
export class EnergyChartComponent implements OnChanges {

  @Input() private device: Device;
  @Input() private fromDate: moment.Moment;
  @Input() private toDate: moment.Moment;

  @ViewChild('energyChart') private chart: BaseChartDirective;

  public labels: moment.Moment[] = [];
  public datasets: Dataset[] = EMPTY_DATASET;
  public loading: boolean = true;

  private ngUnsubscribe: Subject<void> = new Subject<void>();
  private queryreplySubject: Subject<QueryReply>;

  private colors = [{
    backgroundColor: 'rgba(37,154,24,0.2)',
    borderColor: 'rgba(37,154,24,1)',
  }, {
    backgroundColor: 'rgba(221,223,1,0.2)',
    borderColor: 'rgba(221,223,1,1)',
  }, {
    backgroundColor: 'rgba(45,143,171,0.2)',
    borderColor: 'rgba(45,143,171,1)',
  }];
  private options: CHART_OPTIONS;

  ngOnInit() {
    let options = JSON.parse(JSON.stringify(DEFAULT_TIME_CHART_OPTIONS));
    options.scales.yAxes[0].scaleLabel.labelString = "kW";
    this.options = options;
  }

  ngOnChanges(changes: any) {
    // close old queryreplySubject
    if (this.queryreplySubject != null) {
      this.queryreplySubject.complete();
    }
    // show loading...
    this.loading = true;
    // create channels for query
    let channels = this.device.config.getValue().getPowerChannels();
    // execute query
    let queryreplySubject = this.device.query(this.fromDate, this.toDate, channels);
    queryreplySubject.subscribe(queryreply => {
      // prepare datasets and labels
      let activePowers = {
        production: [],
        grid: [],
        consumption: []
      }
      let labels: moment.Moment[] = [];
      for (let reply of queryreply.data) {
        labels.push(moment(reply.time));
        let data = new Summary(this.device.config.getValue(), reply.channels);
        activePowers.grid.push(data.grid.activePower / 1000);
        activePowers.production.push(data.production.activePower / 1000);
        activePowers.consumption.push(data.consumption.activePower / 1000);
      }
      this.datasets = [{
        label: "Erzeugung",
        data: activePowers.production
      }, {
        label: "Netz",
        data: activePowers.grid
      }, {
        label: "Verbrauch",
        data: activePowers.consumption
      }];
      this.labels = labels;
      this.loading = false;
      setTimeout(() => {
        // Workaround, because otherwise chart data and labels are not refreshed...
        if (this.chart) {
          this.chart.ngOnChanges({} as SimpleChanges);
        }
      });

    }, error => {
      this.datasets = EMPTY_DATASET;
      this.labels = [];
      // TODO should be error message
      this.loading = true;
    });
  }

  ngOnDestroy() {
    this.ngUnsubscribe.next();
    this.ngUnsubscribe.complete();
  }
}