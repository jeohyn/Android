using System.Collections.Generic;
using UnityEngine;
using Unity.MLAgents;
using Unity.MLAgents.Sensors;

public class RollerAgent : Agent
{
    Rigidbody rBody;
    void Start()


    {
        rBody = GetComponent<Rigidbody>();
    }

    public Transform Target;

    //heuristic은 손으로 직접 움직이면서 교육시킨단 의미임. 자동으로 컴퓨터로 돌릴경우 필요x
    public override void Heuristic(float[] actionsOut)
    {
        actionsOut[0] = Input.GetAxis("Horizontal");
        actionsOut[1] = Input.GetAxis("Vertical");
    }


    public override void OnEpisodeBegin()
    {
        if (this.transform.localPosition.y < 0)
        {
            // If the Agent fell, zero its momentum
            this.rBody.angularVelocity = Vector3.zero;
            this.rBody.velocity = Vector3.zero;
            this.transform.localPosition = new Vector3(0, 0.5f, 0);
        }

        // Move the target to a new spot
        Target.localPosition = new Vector3(Random.value * 8 - 4,
                                           0.5f,
                                           Random.value * 8 - 4);
    }

    public float forceMultiplier = 10;

    public override void CollectObservations(VectorSensor sensor) //뭘 보고 action을 할지를 적어놓은 코드
    {
        // Target and Agent positions
        sensor.AddObservation(Target.localPosition); //target의 위치(x, y, z)
        sensor.AddObservation(this.transform.localPosition); //agent의 위치(x, y, z)

        // Agent velocity
        sensor.AddObservation(rBody.velocity.x); //x축 가속도(상수 1개)
        sensor.AddObservation(rBody.velocity.z); //z축 가속도(상수 1개)

        //그래서 observation의 벡터는 총 6개.
    }

    public override void OnActionReceived(float[] vectorAction) //여기 안의 내용이 agent의 행동
    {
        // Actions, size = 2
        Vector3 controlSignal = Vector3.zero;
        //상하좌우로만 움직임. 지면의 위아래(y축)으로는 움직이지 x. 이래서 agent의 액션 벡터가 2개임.
        controlSignal.x = vectorAction[0]; //x축
        controlSignal.z = vectorAction[1]; //z축
        rBody.AddForce(controlSignal * forceMultiplier);

        // Rewards
        float distanceToTarget = Vector3.Distance(this.transform.localPosition, Target.localPosition);

        // Reached target
        if (distanceToTarget < 1.42f)
        {
            SetReward(1.0f);
            EndEpisode();
        }

        // Fell off platform
        if (this.transform.localPosition.y < 0)
        {
            //SetReward(-1.0f); 떨어지면 -1점 준다는 의미임.
            EndEpisode();
        }
    }
}
