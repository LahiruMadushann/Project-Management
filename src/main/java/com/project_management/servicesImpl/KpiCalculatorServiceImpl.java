package com.project_management.servicesImpl;

import com.project_management.dto.KpiDTO;
import com.project_management.models.*;
import com.project_management.models.constant.DomainValue;
import com.project_management.models.enums.Domain;
import com.project_management.repositories.EmployeeRepository;
import com.project_management.repositories.PerfectEmployeeRepository;
import com.project_management.services.KpiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class KpiCalculatorServiceImpl implements KpiService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PerfectEmployeeRepository perfectEmployeeRepository;


    @Override
    public List<KpiDTO> calculateKpiByDomain(Domain domain) {
        List<Employee> employees;

        if (domain == Domain.all) {
            // Get all employees including those with null domains
            employees = employeeRepository.findAll();
        } else {
            // Get employees for specific domain
            employees = employeeRepository.findByDomain(domain);
        }

        List<PerfectEmployee> perfectEmployees = perfectEmployeeRepository.findAll();
        Map<String, PerfectEmployee> perfectEmployeeMap = perfectEmployees.stream()
                .collect(Collectors.toMap(PerfectEmployee::getRoleName, pe -> pe));

        return employees.stream()
                .map(employee -> calculateKpiForEmployeeByDomain(employee, perfectEmployeeMap, domain))
                .collect(Collectors.toList());
    }


    /**
     * calculate kpi for project - outer method
     * in here get employees and loop , and send to calculateKpiForEmployee method to calculate single employee
     * kpi and collect to a list
     * @return
     */
    public List<KpiDTO> calculateKpiForAllEmployees() {
        List<Employee> employees = employeeRepository.findAll();
        List<PerfectEmployee> perfectEmployees = perfectEmployeeRepository.findAll();

        Map<String, PerfectEmployee> perfectEmployeeMap = perfectEmployees.stream()
                .collect(Collectors.toMap(
                        PerfectEmployee::getRoleName,
                        pe -> pe,
                        (existing, replacement) -> existing
                ));

        return employees.stream()
                .map(employee -> calculateKpiForEmployee(employee, perfectEmployeeMap))
                .collect(Collectors.toList());
    }

    public KpiDTO calculateKpiForEmployee(String employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        List<PerfectEmployee> perfectEmployees = perfectEmployeeRepository.findAll();

        Map<String, PerfectEmployee> perfectEmployeeMap = perfectEmployees.stream()
                .collect(Collectors.toMap(PerfectEmployee::getRoleName, pe -> pe));

        return calculateKpiForEmployee(employee, perfectEmployeeMap);
    }

    /**
     * calculate employee kpi based on project
     * @param 'project'
     * @param employee
     * @param perfectEmployeeMap
     * @return
     */
    private KpiDTO calculateKpiForEmployee(Employee employee, Map<String, PerfectEmployee> perfectEmployeeMap) {
        PerfectEmployee perfectEmployee = perfectEmployeeMap.get(employee.getRoleName());

        double skillKpi = 0;
        double educationKpi = 0;
        double experienceKpi = 0;

        double skillWeight = 0;
        double educationWeight = 0;
        double experienceWeight = 0;
        double totalWeight = 0;

        if (perfectEmployee != null) {
            skillWeight = calculateWeight(employee.getSkills(), perfectEmployee.getSkills());
            educationWeight = calculateWeight(employee.getEducations(), perfectEmployee.getEducations());
            experienceWeight = calculateWeight(employee.getExperiences(), perfectEmployee.getExperiences());
            totalWeight = skillWeight + educationWeight + experienceWeight;

            /**
             * keep experiences that relevant to the project and remove other experiences
             */
//            List<EmployeeExperience> filteredExperiences = employee.getExperiences().stream()
//                    .filter(exp -> Boolean.parseBoolean(exp.getExperienceName()))
//                    .collect(Collectors.toList());


            skillKpi = (calculateCategoryKpi(employee.getSkills(), perfectEmployee.getSkills())) * ((skillWeight/totalWeight) * 100);
            educationKpi = (calculateCategoryKpi(employee.getEducations(), perfectEmployee.getEducations())) * ((educationWeight/totalWeight) * 100);
            experienceKpi = (calculateCategoryKpi(employee.getExperiences(), perfectEmployee.getExperiences())) * ((experienceWeight/totalWeight) * 100);
        } else {
            // If no perfect employee is found, use default weights and max points
            skillWeight = calculateDefaultWeight(employee.getSkills());
            educationWeight = calculateDefaultWeight(employee.getEducations());
            experienceWeight = calculateDefaultWeight(employee.getExperiences());
            totalWeight = skillWeight + educationWeight + experienceWeight;

            skillKpi = (calculateDefaultCategoryKpi(employee.getSkills())) * ((skillWeight/totalWeight) * 100);
            educationKpi = (calculateDefaultCategoryKpi(employee.getEducations())) * ((educationWeight/totalWeight) * 100);
            experienceKpi = (calculateDefaultCategoryKpi(employee.getExperiences())) * ((experienceWeight/totalWeight) * 100);
        }

        double overallKpi = (skillKpi + educationKpi + experienceKpi);

        KpiDTO kpiDTO = new KpiDTO();
        kpiDTO.setEmployeeId(employee.getEmployeeId());
        kpiDTO.setEmployeeName(employee.getEmployeeName());
        kpiDTO.setRoleName(employee.getRoleName());
        kpiDTO.setRoleCategory(employee.getRoleCategory());
        kpiDTO.setDomain(employee.getDomain());
        kpiDTO.setOverallKpi(overallKpi);
        kpiDTO.setSkillKpi(skillKpi);
        kpiDTO.setEducationKpi(educationKpi);
        kpiDTO.setExperienceKpi(experienceKpi);

        return kpiDTO;
    }

    private KpiDTO calculateKpiForEmployeeByDomain(Employee employee, Map<String, PerfectEmployee> perfectEmployeeMap, Domain domain) {
        PerfectEmployee perfectEmployee = perfectEmployeeMap.get(employee.getRoleName());

        double skillKpi = 0;
        double educationKpi = 0;
        double experienceKpi = 0;

        double skillWeight = 0;
        double educationWeight = 0;
        double experienceWeight = 0;
        double totalWeight = 0;

        if (perfectEmployee != null) {
            skillWeight = calculateWeight(employee.getSkills(), perfectEmployee.getSkills());
            educationWeight = calculateWeight(employee.getEducations(), perfectEmployee.getEducations());
            experienceWeight = calculateWeight(employee.getExperiences(), perfectEmployee.getExperiences());
            totalWeight = skillWeight + educationWeight + experienceWeight;

            /**
             * keep experiences that relevant to the project and remove other experiences
             */
//            List<EmployeeExperience> filteredExperiences = employee.getExperiences().stream()
//                    .filter(exp -> Boolean.parseBoolean(exp.getExperienceName()))
//                    .collect(Collectors.toList());

            if (domain.toString().equals("Health")){
                skillKpi = (calculateCategoryKpi(employee.getSkills(), perfectEmployee.getSkills())) * ((skillWeight/totalWeight) * 100) * DomainValue.Health;
//                educationKpi = (calculateCategoryKpi(employee.getEducations(), perfectEmployee.getEducations())) * ((educationWeight/totalWeight) * 100) * DomainValue.Health;
//                experienceKpi = (calculateCategoryKpi(employee.getExperiences(), perfectEmployee.getExperiences())) * ((experienceWeight/totalWeight) * 100) * DomainValue.Health;
            } else if (domain.toString().equals("Banking")) {
                skillKpi = (calculateCategoryKpi(employee.getSkills(), perfectEmployee.getSkills())) * ((skillWeight/totalWeight) * 100) * DomainValue.Banking;
//                educationKpi = (calculateCategoryKpi(employee.getEducations(), perfectEmployee.getEducations())) * ((educationWeight/totalWeight) * 100) * DomainValue.Banking;
//                experienceKpi = (calculateCategoryKpi(employee.getExperiences(), perfectEmployee.getExperiences())) * ((experienceWeight/totalWeight) * 100) * DomainValue.Banking;
            } else if (domain.toString().equals("Education")) {
                skillKpi = (calculateCategoryKpi(employee.getSkills(), perfectEmployee.getSkills())) * ((skillWeight/totalWeight) * 100) * DomainValue.Finance;
//                educationKpi = (calculateCategoryKpi(employee.getEducations(), perfectEmployee.getEducations())) * ((educationWeight/totalWeight) * 100) * DomainValue.Finance;
//                experienceKpi = (calculateCategoryKpi(employee.getExperiences(), perfectEmployee.getExperiences())) * ((experienceWeight/totalWeight) * 100) * DomainValue.Finance;
            } else if (domain.toString().equals("Ecommerce")) {
                skillKpi = (calculateCategoryKpi(employee.getSkills(), perfectEmployee.getSkills())) * ((skillWeight/totalWeight) * 100) * DomainValue.Ecommerce;
//                educationKpi = (calculateCategoryKpi(employee.getEducations(), perfectEmployee.getEducations())) * ((educationWeight/totalWeight) * 100) * DomainValue.Ecommerce;
//                experienceKpi = (calculateCategoryKpi(employee.getExperiences(), perfectEmployee.getExperiences())) * ((experienceWeight/totalWeight) * 100) * DomainValue.Ecommerce;
            } else {
                skillKpi = (calculateCategoryKpi(employee.getSkills(), perfectEmployee.getSkills())) * ((skillWeight/totalWeight) * 100);
//                educationKpi = (calculateCategoryKpi(employee.getEducations(), perfectEmployee.getEducations())) * ((educationWeight/totalWeight) * 100);
//                experienceKpi = (calculateCategoryKpi(employee.getExperiences(), perfectEmployee.getExperiences())) * ((experienceWeight/totalWeight) * 100);
            }



        } else {
            // If no perfect employee is found, use default weights and max points
            skillWeight = calculateDefaultWeight(employee.getSkills());
            educationWeight = calculateDefaultWeight(employee.getEducations());
            experienceWeight = calculateDefaultWeight(employee.getExperiences());
            totalWeight = skillWeight + educationWeight + experienceWeight;

            if (domain.toString().equals("Health")){
                skillKpi = (calculateDefaultCategoryKpi(employee.getSkills())) * ((skillWeight/totalWeight) * 100) * DomainValue.Health;
//                educationKpi = (calculateDefaultCategoryKpi(employee.getEducations())) * ((educationWeight/totalWeight) * 100) * DomainValue.Health;
//                experienceKpi = (calculateDefaultCategoryKpi(employee.getExperiences())) * ((experienceWeight/totalWeight) * 100) * DomainValue.Health;
            } else if (domain.toString().equals("Banking")) {
                skillKpi = (calculateDefaultCategoryKpi(employee.getSkills())) * ((skillWeight/totalWeight) * 100) * DomainValue.Banking;
//                educationKpi = (calculateDefaultCategoryKpi(employee.getEducations())) * ((educationWeight/totalWeight) * 100) * DomainValue.Banking;
//                experienceKpi = (calculateDefaultCategoryKpi(employee.getExperiences())) * ((experienceWeight/totalWeight) * 100) * DomainValue.Banking;
            } else if (domain.toString().equals("Education")) {
                skillKpi = (calculateDefaultCategoryKpi(employee.getSkills())) * ((skillWeight/totalWeight) * 100) * DomainValue.Education;
//                educationKpi = (calculateDefaultCategoryKpi(employee.getEducations())) * ((educationWeight/totalWeight) * 100) * DomainValue.Education;
//                experienceKpi = (calculateDefaultCategoryKpi(employee.getExperiences())) * ((experienceWeight/totalWeight) * 100) * DomainValue.Education;
            } else if (domain.toString().equals("Ecommerce")) {
                skillKpi = (calculateDefaultCategoryKpi(employee.getSkills())) * ((skillWeight/totalWeight) * 100) * DomainValue.Ecommerce;
//                educationKpi = (calculateDefaultCategoryKpi(employee.getEducations())) * ((educationWeight/totalWeight) * 100) * DomainValue.Ecommerce;
//                experienceKpi = (calculateDefaultCategoryKpi(employee.getExperiences())) * ((experienceWeight/totalWeight) * 100) * DomainValue.Ecommerce;
            } else {
                skillKpi = (calculateDefaultCategoryKpi(employee.getSkills())) * ((skillWeight/totalWeight) * 100);
//                educationKpi = (calculateDefaultCategoryKpi(employee.getEducations())) * ((educationWeight/totalWeight) * 100);
//                experienceKpi = (calculateDefaultCategoryKpi(employee.getExperiences())) * ((experienceWeight/totalWeight) * 100);
            }


        }

        double overallKpi = (skillKpi + educationKpi + experienceKpi);

        KpiDTO kpiDTO = new KpiDTO();
        kpiDTO.setEmployeeId(employee.getEmployeeId());
        kpiDTO.setEmployeeName(employee.getEmployeeName());
        kpiDTO.setRoleName(employee.getRoleName());
        kpiDTO.setRoleCategory(employee.getRoleCategory());
        kpiDTO.setDomain(employee.getDomain());
        kpiDTO.setOverallKpi(overallKpi);
        kpiDTO.setSkillKpi(skillKpi);
        kpiDTO.setEducationKpi(educationKpi);
        kpiDTO.setExperienceKpi(experienceKpi);

        return kpiDTO;
    }

    private <T, P> double calculateCategoryKpi(List<T> employeeAttributes, List<P> perfectAttributes) {
        Map<String, Integer> employeeAttributeMap = createAttributeMap(employeeAttributes);
        Map<String, PerfectAttributeInfo> perfectAttributeMap = createPerfectAttributeMap(perfectAttributes);

        double totalWeight = perfectAttributeMap.values().stream()
                .mapToDouble(PerfectAttributeInfo::getWeight)
                .sum();

        double weightedSum = perfectAttributeMap.entrySet().stream()
                .mapToDouble(entry -> {
                    String name = entry.getKey();
                    PerfectAttributeInfo perfectInfo = entry.getValue();
                    Integer employeePoints = employeeAttributeMap.getOrDefault(name, 0);
                    return (double) employeePoints / perfectInfo.getPoints();
                })
                .sum();

        return weightedSum ;
    }

    private <T, P> double calculateWeight(List<T> employeeAttributes, List<P> perfectAttributes) {
        Map<String, Integer> employeeAttributeMap = createAttributeMap(employeeAttributes);
        Map<String, PerfectAttributeInfo> perfectAttributeMap = createPerfectAttributeMap(perfectAttributes);

        return perfectAttributeMap.values().stream()
                .mapToDouble(PerfectAttributeInfo::getWeight)
                .sum();
    }

    private <T> double calculateDefaultCategoryKpi(List<T> employeeAttributes) {
        int totalPoints = employeeAttributes.stream()
                .mapToInt(this::getAttributePoints)
                .sum();

//        int maxPossiblePoints = employeeAttributes.size() * 10; // Assuming max points is 10 for each attribute
        return (double) totalPoints;
    }
    private <T> double calculateDefaultWeight(List<T> employeeAttributes) {
        return (double) 100 /3;
    }

    private <T> Map<String, Integer> createAttributeMap(List<T> attributes) {
        return attributes.stream()
                .collect(Collectors.toMap(
                        this::getAttributeName,
                        this::getAttributePoints
                ));
    }

    private <P> Map<String, PerfectAttributeInfo> createPerfectAttributeMap(List<P> attributes) {
        return attributes.stream()
                .collect(Collectors.toMap(
                        this::getAttributeName,
                        attr -> new PerfectAttributeInfo(getAttributePoints(attr), getAttributeWeight(attr))
                ));
    }

    private String getAttributeName(Object attribute) {
        if (attribute instanceof EmployeeSkill) {
            return ((EmployeeSkill) attribute).getSkillName();
        } else if (attribute instanceof EmployeeEducation) {
            return ((EmployeeEducation) attribute).getEducationName();
        } else if (attribute instanceof EmployeeExperience) {
            return ((EmployeeExperience) attribute).getExperienceName();
        } else if (attribute instanceof PerfectEmployeeSkill) {
            return ((PerfectEmployeeSkill) attribute).getSkillName();
        } else if (attribute instanceof PerfectEmployeeEducation) {
            return ((PerfectEmployeeEducation) attribute).getEducationName();
        } else if (attribute instanceof PerfectEmployeeExperience) {
            return ((PerfectEmployeeExperience) attribute).getExperienceName();
        }
        throw new IllegalArgumentException("Unsupported attribute type");
    }

    private Integer getAttributePoints(Object attribute) {
        if (attribute instanceof EmployeeSkill) {
            return ((EmployeeSkill) attribute).getSkillPoints();
        } else if (attribute instanceof EmployeeEducation) {
            return ((EmployeeEducation) attribute).getEducationPoints();
        } else if (attribute instanceof EmployeeExperience) {
            return ((EmployeeExperience) attribute).getExperiencePoints();
        } else if (attribute instanceof PerfectEmployeeSkill) {
            return ((PerfectEmployeeSkill) attribute).getSkillPoints();
        } else if (attribute instanceof PerfectEmployeeEducation) {
            return ((PerfectEmployeeEducation) attribute).getEducationPoints();
        } else if (attribute instanceof PerfectEmployeeExperience) {
            return ((PerfectEmployeeExperience) attribute).getExperiencePoints();
        }
        throw new IllegalArgumentException("Unsupported attribute type");
    }

    private Integer getAttributeWeight(Object attribute) {
        if (attribute instanceof PerfectEmployeeSkill) {
            return ((PerfectEmployeeSkill) attribute).getSkillWeight();
        } else if (attribute instanceof PerfectEmployeeEducation) {
            return ((PerfectEmployeeEducation) attribute).getEducationWeight();
        } else if (attribute instanceof PerfectEmployeeExperience) {
            return ((PerfectEmployeeExperience) attribute).getExperienceWeight();
        }
        throw new IllegalArgumentException("Unsupported attribute type");
    }

    private static class PerfectAttributeInfo {
        private final int points;
        private final int weight;

        public PerfectAttributeInfo(int points, int weight) {
            this.points = points;
            this.weight = weight;
        }

        public int getPoints() {
            return points;
        }

        public int getWeight() {
            return weight;
        }
    }


//    private void calculateInitialKpi(Employee employee) {
//        // Fetch the PerfectEmployee based on the given employee's role
//        PerfectEmployee perfectEmployee = perfectEmployeeRepository.findByRoleName(employee.getRoleName());
//        if (perfectEmployee == null) {
//            throw new IllegalArgumentException("Perfect employee not found for role: " + employee.getRoleName());
//        }
//
//        // Initialize total scores
//        double employeeTotal = 0.0;
//        double perfectTotal = 0.0;
//
//        // Calculate Skill Score
//        for (EmployeeSkill employeeSkill : employee.getSkills()) {
//            PerfectEmployeeSkill perfectSkill = perfectEmployee.getSkills().stream()
//                    .filter(s -> s.getSkillName().equalsIgnoreCase(employeeSkill.getSkillName()))
//                    .findFirst()
//                    .orElse(null);
//
//            if (perfectSkill != null) {
//                employeeTotal += employeeSkill.getSkillPoints() * perfectSkill.getSkillWeight();
//                perfectTotal += perfectSkill.getSkillPoints() * perfectSkill.getSkillWeight();
//            }
//        }
//
//        // Calculate Experience Score
//        for (EmployeeExperience employeeExperience : employee.getExperiences()) {
//            PerfectEmployeeExperience perfectExperience = perfectEmployee.getExperiences().stream()
//                    .filter(e -> e.getExperienceName().equalsIgnoreCase(employeeExperience.getExperienceName()))
//                    .findFirst()
//                    .orElse(null);
//
//            if (perfectExperience != null) {
//                employeeTotal += employeeExperience.getExperiencePoints() * perfectExperience.getExperienceWeight();
//                perfectTotal += perfectExperience.getExperiencePoints() * perfectExperience.getExperienceWeight();
//            }
//        }
//
//        // Calculate Education Score
//        for (EmployeeEducation employeeEducation : employee.getEducations()) {
//            PerfectEmployeeEducation perfectEducation = perfectEmployee.getEducations().stream()
//                    .filter(e -> e.getEducationName().equalsIgnoreCase(employeeEducation.getEducationName()))
//                    .findFirst()
//                    .orElse(null);
//
//            if (perfectEducation != null) {
//                employeeTotal += employeeEducation.getEducationPoints() * perfectEducation.getEducationWeight();
//                perfectTotal += perfectEducation.getEducationPoints() * perfectEducation.getEducationWeight();
//            }
//        }
//
//        // Avoid division by zero
//        if (perfectTotal == 0) {
//            employee.setKpi(0.0);
//        }
//
//        // Calculate KPI as a percentage
//        employee.setKpi((employeeTotal / perfectTotal) * 100);
//    }

}
