import {VictoryLabel, VictoryTheme} from "victory-core";
import {VictoryBar, VictoryChart, VictoryPolarAxis, VictoryTooltip} from "victory";
import {memo} from "react";

const NodeChart = (props) => {
    const {buckets, counter} = props
    const max = Math.max.apply(Math, Object.keys(counter).map(function(key) { return counter[key]; }))
    return <VictoryChart polar
                         theme={VictoryTheme.material}
    >
        {
            buckets.map((bucket, i) => {
                return (
                    <VictoryPolarAxis dependentAxis
                                      key={i}
                                      axisLabelComponent={<VictoryLabel
                                          events={{
                                              onMouseOver: (evt) => {
                                                  evt.target.innerHTML = `${bucket.info.replace("tcp_port:", "")}`
                                              },
                                              onMouseLeave: (evt) => {
                                                  evt.target.innerHTML = `${bucket.hashKey}`
                                              }
                                          }}
                                      />}
                                      label={`${bucket.hashKey}`}
                                      labelPlacement="perpendicular"
                                      style={{
                                          tickLabels: {fill: "none"},
                                          axisLabel: {
                                              fontSize: 6,
                                              fill: bucket.color
                                          }
                                      }}
                                      animate={{
                                          duration: 200,
                                          easing: "bounce"
                                      }}
                                      axisValue={`${bucket.hashKey}`}
                    />
                );
            })
        }
        <VictoryBar
            style={
                {
                    data: {
                        fill: "tomato",opacity: ({ datum }) => datum.y/max, width: 10, fontcolor: 'white'
                    },
                    labels: {
                        fill: "black", fontSize: 4
                    }
                }
            }
            labelComponent={<VictoryTooltip/>}
            labels={({datum}) => datum.y}
            data={
                buckets.map((bucket) => {
                    const serverKey = `${bucket.hashKey}`
                    return {x: serverKey, y: counter[serverKey] || 0}
                })
            }
        />
    </VictoryChart>
}

export default memo(NodeChart)
