import {VictoryLabel, VictoryTheme} from "victory-core";
import {VictoryBar, VictoryChart, VictoryPolarAxis} from "victory";
import {memo} from "react";

const NodeChart = (props) => {
    const {buckets, counter} = props
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
                                          duration: 1000,
                                          easing: "bounce"
                                      }}
                                      axisValue={`${bucket.hashKey}`}
                    />
                );
            })
        }
        <VictoryBar
            style={{data: {fill: "tomato", width: 10}, labels: {fill: "black"}}}
            animate={{
                duration: 500,
                easing: "bounce"
            }}
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
