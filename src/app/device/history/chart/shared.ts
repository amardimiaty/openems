export type CHART_OPTIONS = {
    maintainAspectRatio: boolean,
    legend: {
        position: "bottom"
    },
    elements: {
        point: {
            radius: number,
            hitRadius: number,
            hoverRadius: number
        }
    },
    scales: {
        yAxes: [{
            scaleLabel: {
                display: boolean,
                labelString: string
            },
            ticks: {
                beginAtZero: boolean,
                max?: number
            }
        }],
        xAxes: [{
            type: "time",
            time: {
                displayFormats: {
                    millisecond: string,
                    second: string,
                    minute: string,
                    hour: string,
                    day: string,
                    week: string,
                    month: string,
                    quarter: string,
                    year: string
                }
            }
        }]
    }
}

export const DEFAULT_TIME_CHART_OPTIONS: CHART_OPTIONS = {
    maintainAspectRatio: false,
    legend: {
        position: 'bottom'
    },
    elements: {
        point: {
            radius: 0,
            hitRadius: 10,
            hoverRadius: 10
        }
    },
    scales: {
        yAxes: [{
            scaleLabel: {
                display: true,
                labelString: ""
            },
            ticks: {
                beginAtZero: true
            }
        }],
        xAxes: [{
            type: 'time',
            time: {
                displayFormats: {
                    millisecond: 'SSS [ms]',
                    second: 'HH:mm:ss a', // 17:20:01
                    minute: 'HH:mm', // 17:20
                    hour: 'HH:mm', // 17:20
                    day: 'll', // Sep 4 2015
                    week: 'll', // Week 46, or maybe "[W]WW - YYYY" ?
                    month: 'MMM YYYY', // Sept 2015
                    quarter: '[Q]Q - YYYY', // Q3
                    year: 'YYYY' // 2015
                }
            }
        }]
    }
};