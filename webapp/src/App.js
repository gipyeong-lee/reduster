import './App.css';
import {useEffect, useState} from "react";
import {Container, Form, Grid} from "semantic-ui-react";
import axios from "axios";
import {VictoryPolarAxis} from 'victory';
import {VictoryTheme} from "victory-core";

function App() {
    const [isInit, setIsInit] = useState(false)
    const [buckets, setBuckets] = useState([])
    const [server, setServer] = useState({host: '127.0.0.1', port: '6379'})

    useEffect(() => {
        loadBuckets(setBuckets, setIsInit)
    }, [isInit])

    function handleAddServer() {
        addServer(server, setIsInit)
    }

    function handleRemoveServer() {
        removeServer(server, setIsInit)
    }

    function inputChange(event) {
        setServer(Object.assign({...server}, {[event.target.name]: event.target.value}))
    }

    return (
        <Container textAlign={'center'}>
            <Grid centered>
                <Grid.Row columns={12}>
                    <Grid.Column width={8}>
                        <Form>
                            <Form.Group>
                                <Form.Input
                                    onChange={inputChange}
                                    name={'host'}
                                    placeholder='127.0.0.1'
                                    defaultValue={server.host}
                                />
                                <Form.Input
                                    onChange={inputChange}
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
                        <svg width={500} height={500} >
                            <VictoryPolarAxis
                                domain={[0, 1024]}
                                animate={{
                                    duration: 500,
                                    easing: "bounce"
                                }}
                                width={500}
                                height={500}
                                tickValues={buckets.map((bucket) => bucket.hashKey)}
                                tickFormat={(hashKey) => {
                                    return `${Number(hashKey).toFixed(0)}`
                                }}
                                theme={VictoryTheme.material}
                                standalone={false}
                            />
                        </svg>
                    </Grid.Column>
                </Grid.Row>
            </Grid>
        </Container>
    );
}

function loadBuckets(setBuckets, setIsInit) {
    axios.get("http://localhost:8080/api/v1/redis/server/info/all")
        .then(({data}) => {
            setBuckets(data.buckets)
            setIsInit(true)
        })
}

function addServer(server, setIsInit) {
    axios.post("http://localhost:8080/api/v1/redis/server", server).then(() => {
        setIsInit(false)
    })
}

function removeServer(server, setIsInit) {
    axios.delete("http://localhost:8080/api/v1/redis/server", {data: server}).then(() => {
        setIsInit(false)
    })
}

export default App;
