import './App.css';
import {memo, useEffect, useState} from "react";
import {Container, Form, Grid} from "semantic-ui-react";
import axios from "axios";
import {VictoryBar, VictoryChart, VictoryPolarAxis} from 'victory';
import {VictoryTheme} from "victory-core";

function App() {
    const [state, setState] = useState({
        buckets: [], items: [], counter: {}, isInit: true
    })
    const [server, setServer] = useState({host: '127.0.0.1', port: '6379'})

    const {buckets, items, counter, isInit} = state

    useEffect(() => {
        if (isInit === false)
            loadBuckets(state, setState)
    }, [isInit])

    function handleAddServer() {
        addServer(server, state, setState)
    }

    function handleRemoveServer() {
        removeServer(server, state, setState)
    }

    function inputChange(event) {
        setServer({...server, ...{[event.target.name]: event.target.value}})
    }
    console.log(state)
    return (
        <Container textAlign={'center'}>
            <Grid centered>
                <Grid.Row columns={12}>
                    <Grid.Column width={8}>
                        <Form>
                            <Form.Group>
                                <Form.Input
                                    onChange={(event) => {
                                        inputChange(event)
                                    }}
                                    name={'host'}
                                    placeholder='127.0.0.1'
                                    defaultValue={server.host}
                                />
                                <Form.Input
                                    onChange={(event) => {
                                        inputChange(event)
                                    }}
                                    name={'port'}
                                    placeholder='6379'
                                    defaultValue={server.port}
                                />
                                <Form.Button content={'Add'} onClick={handleAddServer}/>
                                <Form.Button content={'remove'} color={'red'} onClick={handleRemoveServer}/>
                            </Form.Group>
                        </Form>
                    </Grid.Column>
                    <Grid.Column width={3}>

                    </Grid.Column>
                </Grid.Row>
                <Grid.Row columns={12}>
                    <Grid.Column width={12} textAlign={'center'}>
                        {buckets.length > 0 ?
                            <VictoryChart polar
                                          theme={VictoryTheme.material}
                            >
                                {
                                    buckets.map((bucket, i) => {
                                        return (
                                            <VictoryPolarAxis dependentAxis
                                                              key={i}
                                                              label={`${bucket.hashKey}`}
                                                              labelPlacement="perpendicular"
                                                              style={{tickLabels: {fill: "none"}}}
                                                              axisValue={`${bucket.hashKey}`}
                                            />
                                        );
                                    })
                                }
                                <VictoryBar
                                    style={{data: {fill: "tomato", width: 25}}}
                                    data={
                                        buckets.map((bucket) => {
                                            const serverKey = `${bucket.hashKey}`
                                            return {x: serverKey, y: counter[serverKey] || 0}
                                        })
                                    }
                                />
                            </VictoryChart> : ''}
                    </Grid.Column>
                </Grid.Row>
            </Grid>
        </Container>
    );
}

async function loadBuckets(state, setState) {
    const bucketResponse = await axios.get("http://localhost:8080/api/v1/redis/server/info/all")
    const itemResponse = await axios.get("http://localhost:8080/api/v1/redis/keys/info/all")
    const counter = makeCounter(itemResponse.data.keys)
    console.log(bucketResponse.data.buckets)
    setState({
        ...state, ...{
            buckets: bucketResponse.data.buckets,
            items: itemResponse.data.keys,
            counter: counter,
            isInit: true
        }
    })
}

function makeCounter(items) {
    const nextCounter = {}
    items.forEach((item) => {
        if (!nextCounter[`${item.serverKey}`]) {
            nextCounter[`${item.serverKey}`] = 1
        } else {
            nextCounter[`${item.serverKey}`] += 1
        }
    })

    return nextCounter
}

function addServer(server, state, setState) {
    console.log('addServer', server)
    axios.post("http://localhost:8080/api/v1/redis/server", server).then(() => {
        setState({...state, ...{isInit: false}})
    })
}

function removeServer(server, state, setState) {
    console.log('removeServer', server)
    axios.delete("http://localhost:8080/api/v1/redis/server", {data: server}).then(() => {
        setState({...state, ...{isInit: false}})
    })
}

export default memo(App);
